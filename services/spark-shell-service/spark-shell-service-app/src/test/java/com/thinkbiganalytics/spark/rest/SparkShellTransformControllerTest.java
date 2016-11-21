package com.thinkbiganalytics.spark.rest;

import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.script.ScriptException;
import javax.ws.rs.core.Response;

public class SparkShellTransformControllerTest {

    /** Verify requesting a Spark transformation. */
    @Test
    public void create() throws Exception {
        // Mock transform objects
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");

        TransformResponse transformResponse = new TransformResponse();
        transformResponse.setProgress(0.0);
        transformResponse.setStatus(TransformResponse.Status.PENDING);
        transformResponse.setTable("results");

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.execute(transformRequest)).thenReturn(transformResponse);

        // Test transforming
        SparkShellTransformController controller = new SparkShellTransformController();
        controller.transformService = transformService;

        Response response = controller.create(transformRequest);
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
        Assert.assertEquals(transformResponse, response.getEntity());
    }

    /** Verify response if missing parent script. */
    @Test
    public void createWithMissingParentScript() {
        // Create transform request
        TransformRequest request = new TransformRequest();
        request.setScript("parent");
        request.setParent(new TransformRequest.Parent());

        // Test missing parent script
        SparkShellTransformController controller = new SparkShellTransformController();
        Response response = controller.create(request);
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse) response.getEntity();
        Assert.assertEquals("The parent must include a script with the transformations performed.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if missing parent table. */
    @Test
    public void createWithMissingParentTable() {
        // Create transform request
        TransformRequest request = new TransformRequest();
        request.setScript("parent");

        TransformRequest.Parent parent = new TransformRequest.Parent();
        parent.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");
        request.setParent(parent);

        // Test missing parent table
        SparkShellTransformController controller = new SparkShellTransformController();
        Response response = controller.create(request);
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse) response.getEntity();
        Assert.assertEquals("The parent must include the table containing the results.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if missing script. */
    @Test
    public void createWithMissingScript() {
        SparkShellTransformController controller = new SparkShellTransformController();
        Response response = controller.create(new TransformRequest());
        Assert.assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());

        TransformResponse entity = (TransformResponse) response.getEntity();
        Assert.assertEquals("The request must include a script with the transformations to perform.", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify response if a script exception is thrown. */
    @Test
    public void createWithScriptException() throws Exception {
        // Create transform objects
        TransformRequest request = new TransformRequest();
        request.setScript("sqlContext.sql(\"SELECT * FROM invalid\")");

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.execute(request)).thenThrow(new ScriptException("Invalid script"));

        // Test script exception
        SparkShellTransformController controller = new SparkShellTransformController();
        controller.transformService = transformService;

        Response response = controller.create(request);
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());

        TransformResponse entity = (TransformResponse) response.getEntity();
        Assert.assertEquals("Invalid script", entity.getMessage());
        Assert.assertEquals(TransformResponse.Status.ERROR, entity.getStatus());
    }

    /** Verify requesting a transformation status. */
    @Test
    public void getTable() throws Exception {
        // Mock transform objects
        TransformJob pendingJob = Mockito.mock(TransformJob.class);
        Mockito.when(pendingJob.groupId()).thenReturn("PendingJob");
        Mockito.when(pendingJob.progress()).thenReturn(0.5);

        TransformJob successJob = Mockito.mock(TransformJob.class);
        TransformResponse successResponse = new TransformResponse();
        Mockito.when(successJob.get()).thenReturn(successResponse);
        Mockito.when(successJob.isDone()).thenReturn(true);

        TransformService transformService = Mockito.mock(TransformService.class);
        Mockito.when(transformService.getJob("PendingJob")).thenReturn(pendingJob);
        Mockito.when(transformService.getJob("SuccessJob")).thenReturn(successJob);

        // Test with pending job
        SparkShellTransformController controller = new SparkShellTransformController();
        controller.transformService = transformService;

        Response response = controller.getTable("PendingJob");
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());

        TransformResponse transformResponse = (TransformResponse)response.getEntity();
        Assert.assertEquals(0.5, transformResponse.getProgress(), 0.001);
        Assert.assertEquals(TransformResponse.Status.PENDING, transformResponse.getStatus());
        Assert.assertEquals("PendingJob", transformResponse.getTable());

        // Test with success job
        response = controller.getTable("SuccessJob");
        Assert.assertEquals(successResponse, response.getEntity());
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
    }
}
