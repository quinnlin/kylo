package com.thinkbiganalytics.security.rest.controller;

/*-
 * #%L
 * thinkbig-security-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Obtain and manage access control information for users and groups.
 */
@Component
@Api(tags = "Security - Access Control", produces = "application/json")
@Path("/v1/security/actions")
@SwaggerDefinition(tags = @Tag(name = "Security - Access Control", description = "manage access controls"))
public class AccessControlController {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private AllowedModuleActionsProvider actionsProvider;

    @Inject
    @Named("actionsModelTransform")
    private ActionsModelTransform actionsTransform;

    @GET
    @Path("{name}/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "The given name was not found.", response = RestResponseStatus.class)
                  })
    public ActionGroup getAvailableActions(@PathParam("name") String moduleName) {
        return metadata.read(() -> {
            return actionsProvider.getAvailableActions(moduleName)
                .map(this.actionsTransform.availableActionsToActionSet("services"))
                .orElseThrow(() -> new WebApplicationException("The available service actions were not found",
                                                               Status.NOT_FOUND));
        });
    }

    @GET
    @Path("{name}/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of allowed actions for a principal.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "The given name was not found.", response = RestResponseStatus.class)
                  })
    public ActionGroup getAllowedActions(@PathParam("name") String moduleName,
                                         @QueryParam("user") Set<String> userNames,
                                         @QueryParam("group") Set<String> groupNames) {
        Set<Principal> users = this.actionsTransform.toUserPrincipals(userNames);
        Set<Principal> groups = this.actionsTransform.toGroupPrincipals(groupNames);
        Principal[] principals = Stream.concat(users.stream(), groups.stream()).toArray(Principal[]::new);

        return metadata.read(() -> {
            return actionsProvider.getAllowedActions(moduleName)
                .map(this.actionsTransform.availableActionsToActionSet("services"))
                .orElseThrow(() -> new WebApplicationException("The available service actions were not found",
                                                               Status.NOT_FOUND));
        }, principals);
    }

    @POST
    @Path("{name}/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Modifies the permissions of a principal.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated permissions.", response = ActionGroup.class),
                      @ApiResponse(code = 500, message = "The permissions could not be changed.", response = RestResponseStatus.class)
                  })
    public ActionGroup postPermissionsChange(@PathParam("name") String moduleName,
                                             PermissionsChange changes) {
        Set<Action> actionSet = collectActions(changes);
        Set<Principal> principals = collectPrincipals(changes);
        final Consumer<Principal> permChange;

        switch (changes.getChange()) {
            case ADD:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.enable(principal, actionSet));
                });
                break;
            case REMOVE:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.disable(principal, actionSet));
                });
                break;
            default:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.enableOnly(principal, actionSet));
                });
        }

        metadata.commit(() -> {
            principals.stream().forEach(permChange);
            return null;
        });

        return getAllowedActions(moduleName, changes.getUsers(), changes.getGroups());
    }


    @GET
    @Path("{name}/change/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the permissions that may be changed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the permissions.", response = PermissionsChange.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The given name was not found.", response = RestResponseStatus.class)
                  })
    public PermissionsChange getAllowedPermissionsChange(@PathParam("name") String moduleName,
                                                         @QueryParam("type") String changeType,
                                                         @QueryParam("user") Set<String> users,
                                                         @QueryParam("group") Set<String> groups) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Status.BAD_REQUEST);
        }

        return metadata.read(() -> {
            return actionsProvider.getAvailableActions(moduleName)
                .map(this.actionsTransform.availableActionsToPermissionsChange(ChangeType.valueOf(changeType.toUpperCase()), moduleName, users, groups))
                .orElseThrow(() -> new WebApplicationException("The available service actions were not found",
                                                               Status.NOT_FOUND));
        });
    }


    private Set<Principal> collectPrincipals(PermissionsChange changes) {
        Set<Principal> set = new HashSet<>();

        set.addAll(this.actionsTransform.toUserPrincipals(changes.getUsers()));
        set.addAll(this.actionsTransform.toGroupPrincipals(changes.getGroups()));

        return set;
    }

    /**
     * Creates a set of domain actions from the REST model actions.  The resulting set will
     * contain only the leaf actions from domain action hierarchy.
     */
    private Set<Action> collectActions(PermissionsChange changes) {
        Set<Action> set = new HashSet<>();

        for (com.thinkbiganalytics.security.rest.model.Action modelAction : changes.getActionSet().getActions()) {
            loadActionSet(modelAction, Action.create(modelAction.getSystemName(), modelAction.getTitle(), modelAction.getDescription()), set);
        }

        return set;
    }

    /**
     * Adds an new domain action to the set if the REST model action represents a leaf of the action hierarchy.
     * Otherwise, it loads the child actions recursively.
     */
    private void loadActionSet(com.thinkbiganalytics.security.rest.model.Action modelAction, Action action, Set<Action> set) {
        if (modelAction.getActions().isEmpty()) {
            set.add(action);
        } else {
            for (com.thinkbiganalytics.security.rest.model.Action modelChild : modelAction.getActions()) {
                loadActionSet(modelChild, action.subAction(modelChild.getSystemName(), modelChild.getTitle(), modelChild.getDescription()), set);
            }
        }
    }

}
