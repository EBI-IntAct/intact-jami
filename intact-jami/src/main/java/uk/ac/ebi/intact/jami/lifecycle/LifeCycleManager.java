/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
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
 */
package uk.ac.ebi.intact.jami.lifecycle;

import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.jami.lifecycle.status.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

/**
 * Lifecycle manager.
 *
 * @author Marine Dumousseau (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "jamiLifeCycleManager")
public class LifeCycleManager {

    @Resource(name = "jamiStartStatus")
    private StartStatus jamiStartStatus;
    @Resource(name = "jamiNewStatus")
    private NewStatus jamiNewStatus;
    @Resource(name = "jamiAssignedStatus")
    private AssignedStatus jamiAssignedStatus;
    @Resource(name = "jamiCurationInProgressStatus")
    private CurationInProgressStatus jamiCurationInProgressStatus;
    @Resource(name = "jamiReadyForCheckingStatus")
    private ReadyForCheckingStatus jamiReadyForCheckingStatus;
    @Resource(name = "jamiAcceptedStatus")
    private AcceptedStatus jamiAcceptedStatus;
    @Resource(name = "jamiAcceptedOnHoldStatus")
    private AcceptedOnHoldStatus jamiAcceptedOnHoldStatus;
    @Resource(name = "jamiReadyForReleaseStatus")
    private ReadyForReleaseStatus jamiReadyForReleaseStatus;
    @Resource(name = "jamiReleasedStatus")
    private ReleasedStatus jamiReleasedStatus;

    private Collection<GlobalStatus> getAllStatus() {
        return Arrays.asList(jamiStartStatus, jamiNewStatus, jamiAssignedStatus, jamiCurationInProgressStatus, jamiReadyForCheckingStatus,
                jamiAcceptedStatus, jamiAcceptedOnHoldStatus, jamiReadyForReleaseStatus, jamiReleasedStatus);
    }

    public void registerListener( LifecycleEventListener listener ) {
        final Collection<GlobalStatus> allStatus = getAllStatus();
        for ( GlobalStatus status : allStatus ) {
            System.out.println( status.getClass().getSimpleName() + ".registerListener("+ listener.getClass().getSimpleName() +")" );
            status.registerListener( listener );
        }
    }

    public void removeListener( LifecycleEventListener listener ) {
        final Collection<GlobalStatus> allStatus = getAllStatus();
        for ( GlobalStatus status : allStatus ) {
            System.out.println( status.getClass().getSimpleName() + ".removeListener("+ listener.getClass().getSimpleName() +")" );
            status.removeListener( listener );
        }
    }

    public void removeAllListeners(){
        final Collection<GlobalStatus> allStatus = getAllStatus();
        for ( GlobalStatus status : allStatus ) {
            status.removeAllListeners();
        }
    }

    public GlobalStatus getGlobalStatus() {
        // return any of the implementations that does not override any of its methods
        return jamiStartStatus;
    }

    public StartStatus getStartStatus() {
        return jamiStartStatus;
    }

    public NewStatus getNewStatus() {
        return jamiNewStatus;
    }

    public AssignedStatus getAssignedStatus() {
        return jamiAssignedStatus;
    }

    public CurationInProgressStatus getCurationInProgressStatus() {
        return jamiCurationInProgressStatus;
    }

    public ReadyForCheckingStatus getReadyForCheckingStatus() {
        return jamiReadyForCheckingStatus;
    }

    public AcceptedStatus getAcceptedStatus() {
        return jamiAcceptedStatus;
    }

    public AcceptedOnHoldStatus getAcceptedOnHoldStatus() {
        return jamiAcceptedOnHoldStatus;
    }

    public ReadyForReleaseStatus getReadyForReleaseStatus() {
        return jamiReadyForReleaseStatus;
    }

    public ReleasedStatus getReleasedStatus() {
        return jamiReleasedStatus;
    }
}
