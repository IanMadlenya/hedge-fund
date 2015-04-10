/**
 * This code was generated from {@link com.onenow.workflow.SummitWorkflow}.
 *
 * Any changes made directly to this file will be lost when 
 * the code is regenerated.
 */
package com.onenow.workflow;

import com.amazonaws.services.simpleworkflow.flow.DataConverter;
import com.amazonaws.services.simpleworkflow.flow.StartWorkflowOptions;
import com.amazonaws.services.simpleworkflow.flow.WorkflowClientBase;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.generic.GenericWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.onenow.aws.SummitWorkflow;

class SummitWorkflowClientImpl extends WorkflowClientBase implements SummitWorkflowClient {

    public SummitWorkflowClientImpl(WorkflowExecution workflowExecution, WorkflowType workflowType,  
            StartWorkflowOptions options, DataConverter dataConverter, GenericWorkflowClient genericClient) {
        super(workflowExecution, workflowType, options, dataConverter, genericClient);
    }
    
    @Override
    public final Promise<Void> mainFlow() {
        return mainFlow((StartWorkflowOptions)null);
    }

    @Override
    public final Promise<Void> mainFlow(Promise<?>... waitFor) {
        return mainFlow((StartWorkflowOptions)null, waitFor);
    }
    
    @Override
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Promise<Void> mainFlow(StartWorkflowOptions optionsOverride, Promise<?>... waitFor) {
        return (Promise) startWorkflowExecution(new Object[0], optionsOverride, Void.class, waitFor);
    }
    	

}