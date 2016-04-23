package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 10/16/15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "expand",
        "id",
        "self",
        "key",
        "fields"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetIssue {

    @JsonProperty("expand")
    private String expand;
    @JsonProperty("id")
    private String id;
    @JsonProperty("self")
    private String self;
    @JsonProperty("key")
    private String key;
    @JsonProperty("fields")
    private Fields fields;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();




    /**
     *
     * @return
     *     The expand
     */
    @JsonProperty("expand")
    public String getExpand() {
        return expand;
    }

    /**
     *
     * @param expand
     *     The expand
     */
    @JsonProperty("expand")
    public void setExpand(String expand) {
        this.expand = expand;
    }

    /**
     *
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     *     The self
     */
    @JsonProperty("self")
    public String getSelf() {
        return self;
    }

    /**
     *
     * @param self
     *     The self
     */
    @JsonProperty("self")
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     *
     * @return
     *     The key
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key
     *     The key
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     *
     * @return
     *     The fields
     */
    @JsonProperty("fields")
    public Fields getFields() {
        return fields;
    }

    /**
     *
     * @param fields
     *     The fields
     */
    @JsonProperty("fields")
    public void setFields(Fields fields) {
        this.fields = fields;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}