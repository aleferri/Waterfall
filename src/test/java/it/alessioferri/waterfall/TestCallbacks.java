/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package it.alessioferri.waterfall;

import java.util.Collection;
import java.util.HashMap;
/*-
 * #%L
 * Waterfall
 * %%
 * Copyright (C) 2023 Alessio Ferri
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Alessio
 */
public record TestCallbacks(Supplier<Long> taskIds, Map<Long, TaskSnapshot> updates)
        implements TaskScheduler<StageKind, Stage, TestLink>, Dispatcher<StageKind, Stage, TestLink> {

    public static TestCallbacks of(Supplier<Long> taskIds) {
        return new TestCallbacks( taskIds, new HashMap<>() );
    }

    @Override
    public StageStatus<StageKind, Stage, TestLink> onDepsUpdates(TasksWave<StageKind, Stage, TestLink> wave,
            Stage stage, List<Long> deps) {
        switch ( stage.kind() ) {
        case START -> {
            return new StageStatus<>( wave, true );
        }
        case EXECUTE_ONLY_IF_ALL_FAIL -> {
            var depsInfo = wave.queryDependenciesInfo( deps );

            var allFailed = depsInfo.allDidResolveAs( TaskResult.FAIL );

            if ( depsInfo.allDidResolve() && !allFailed ) {
                wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
            }

            return new StageStatus<>( wave, allFailed );
        }
        case EXECUTE_ONLY_IF_ALL_SUCCESS -> {
            var depsInfo = wave.queryDependenciesInfo( deps );

            boolean allSuccess = depsInfo.allDidResolveAs( TaskResult.SUCCESS );

            if ( depsInfo.allDidResolve() && !allSuccess ) {
                wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
            }

            return new StageStatus<>( wave, allSuccess );
        }
        case EXECUTE_ONLY_IF_ANY_SUCCESS -> {
            var depsInfo = wave.queryDependenciesInfo( deps );

            boolean anySuccess = depsInfo.anyDidResolveAs( TaskResult.SUCCESS );

            if ( depsInfo.allDidResolve() && !anySuccess ) {
                wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
            }

            return new StageStatus<>( wave, anySuccess );
        }
        case EXECUTE_ONLY_IF_ANY_FAIL -> {
            var depsInfo = wave.queryDependenciesInfo( deps );

            boolean anyFail = depsInfo.anyDidResolveAs( TaskResult.SUCCESS );

            if ( depsInfo.allDidResolve() && !anyFail ) {
                wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
            }

            return new StageStatus<>( wave, anyFail );
        }
        case END -> {
            return new StageStatus<>( wave, true );
        }
        default -> throw new AssertionError( stage.kind().name() );
        }
    }

    @Override
    public TaskSnapshot scheduleTaskFor(TasksWave<StageKind, Stage, TestLink> wave, Stage stage,
            Collection<TestLink> incomings, Delay delay) {

        Delay delaySum = delay;

        switch ( stage.kind() ) {
        case EXECUTE_ONLY_IF_ALL_FAIL, EXECUTE_ONLY_IF_ANY_FAIL -> {
            delaySum = delay.add( wave.selectDelayFor( stage, stage.delayPolicy(), incomings, TaskResult.FAIL ) );
        }
        case EXECUTE_ONLY_IF_ALL_SUCCESS, EXECUTE_ONLY_IF_ANY_SUCCESS -> {
            delaySum = delay.add( wave.selectDelayFor( stage, stage.delayPolicy(), incomings, TaskResult.SUCCESS ) );
        }
        default -> {
            delaySum = delay;
        }
        }

        if ( !delaySum.isNone() ) {
            return TaskSnapshot.scheduledLater( this.taskIds.get(), stage.stageId(), delaySum );
        }

        if ( stage instanceof ImmediateFail ) {
            return TaskSnapshot.failed( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof ImmediateSuccess ) {
            return TaskSnapshot.succeeded( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof DeferredSuccess ) {
            return TaskSnapshot.scheduledNow( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof DeferredFail ) {
            return TaskSnapshot.scheduledNow( this.taskIds.get(), stage.stageId() );
        }

        return TaskSnapshot.skipped( this.taskIds.get(), stage.stageId() );
    }

    public TaskSnapshot advanceTask(long taskId, Stage stage) {
        if ( stage instanceof DeferredSuccess ) {
            return TaskSnapshot.succeeded( taskId, stage.stageId() );
        } else if ( stage instanceof DeferredFail ) {
            return TaskSnapshot.failed( taskId, stage.stageId() );
        }

        throw new RuntimeException( "cannot advance a finished task" );
    }

    @Override
    public TaskScheduler<StageKind, Stage, TestLink> schedulerFor(StageKind kind) {
        return this;
    }

    @Override
    public Optional<WaveStartData<StageKind, Stage>> onBackwardLinkUpdate(TasksWave<StageKind, Stage, TestLink> wave,
            Stage stage, Collection<TestLink> incomings, long linkDep) {
        switch ( stage.kind() ) {
        case START, END -> {
            return Optional.of( WaveStartData.prepare( wave.waveId(), DelayDate.none(), stage ) );
        }
        case EXECUTE_ONLY_IF_ALL_FAIL, EXECUTE_ONLY_IF_ALL_SUCCESS -> {
            return Optional.empty();
        }
        case EXECUTE_ONLY_IF_ANY_SUCCESS -> {

            if ( wave.hasRelatedTask( linkDep ) ) {
                var snapshot = wave.snapshotOfStage( linkDep );
                var anySuccess = snapshot.status().isFinished() && snapshot.result() == TaskResult.SUCCESS;

                if ( anySuccess ) {
                    return Optional.of( WaveStartData.prepare( wave.waveId(), DelayDate.none(), stage ) );
                }

            }

            return Optional.empty();
        }
        case EXECUTE_ONLY_IF_ANY_FAIL -> {

            if ( wave.hasRelatedTask( linkDep ) ) {
                var snapshot = wave.snapshotOfStage( linkDep );
                var anyFail = snapshot.status().isFinished() && snapshot.result() == TaskResult.FAIL;

                if ( anyFail ) {
                    return Optional.of( WaveStartData.prepare( wave.waveId(), DelayDate.none(), stage ) );
                }

            }

            return Optional.empty();
        }
        default -> throw new AssertionError( stage.kind().name() );
        }
    }

    @Override
    public TaskSnapshot takeSnapshot(TasksWave<StageKind, Stage, TestLink> wave, Stage stage, long taskId) {
        var currentSnapshot = wave.snapshotOfTask( taskId );
        if ( !currentSnapshot.status().isActive() ) {
            return currentSnapshot;
        }

        return this.advanceTask( taskId, stage );
    }

}
