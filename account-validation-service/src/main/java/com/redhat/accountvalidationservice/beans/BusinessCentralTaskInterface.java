package com.redhat.accountvalidationservice.beans;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public interface BusinessCentralTaskInterface {

   @PUT
   @Path("/services/rest/server/containers/TransactionAnalytics_1.0.0-SNAPSHOT/cases/instances/{caseId}/tasks/Account%20Validation%20Checks")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)

   public void triggerAdhocTask(@PathParam("caseId") String caseId, String body);
}
