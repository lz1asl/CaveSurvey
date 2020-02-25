package com.astoev.cave.survey.activity.dialog;

/**
 * Interface for activities that will listen for response from ConfirmationDialog
 *
 * @author Zhivko Mitrev
 */
public interface ConfirmationHandler {

    /**
     * Call back triggered from ConfirmationDialog in case of confirmed operation
     *
     * @param operationArg - confirmedOperation
     * @return true if operation handled
     */
    boolean confirmOperation(ConfirmationOperation operationArg);
}
