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
import java.util.function.Supplier;

/**
 *
 * @author Alessio
 */
public record TestCallbacks(Supplier<Long> taskIds) implements CallbacksTable<StageKind, Stage>, Dispatcher<StageKind, Stage> {

    @Override
    public StageStatus onDepsUpdates(TasksWave wave, Stage stage, List<Long> deps) {
        switch ( stage.kind() ) {
            case START -> {
                return new StageStatus( wave, true );
            }
            case EXECUTE_ONLY_IF_ALL_FAIL -> {
                boolean allFailed = true;
                boolean depsResolved = true;

                for ( var dep : deps ) {
                    if ( wave.hasRelatedTask( dep ) ) {
                        var snapshot = wave.snapshotOfStage( dep );
                        allFailed = allFailed && snapshot.status().isFinished() && snapshot.result() == TaskResult.FAIL;
                    } else {
                        allFailed = false;
                        depsResolved = false;
                    }
                }

                if ( depsResolved && !allFailed ) {
                    wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
                }

                if ( allFailed && !stage.delay().isNone() ) {
                    return new StageStatus( TasksWaveDefault.<StageKind, Stage, TestLink>initWave( -1, wave.waveId() ), allFailed );
                }

                return new StageStatus( wave, allFailed );
            }
            case EXECUTE_ONLY_IF_ALL_SUCCESS -> {
                boolean allSuccess = true;
                boolean depsResolved = true;

                for ( var dep : deps ) {
                    if ( wave.hasRelatedTask( dep ) ) {
                        var snapshot = wave.snapshotOfStage( dep );
                        allSuccess = allSuccess && snapshot.status().isFinished() && snapshot.result() == TaskResult.SUCCESS;
                    } else {
                        allSuccess = false;
                        depsResolved = false;
                    }
                }

                if ( depsResolved && !allSuccess ) {
                    wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
                }

                if ( allSuccess && !stage.delay().isNone() ) {
                    return new StageStatus( TasksWaveDefault.<StageKind, Stage, TestLink>initWave( -1, wave.waveId() ), allSuccess );
                }

                return new StageStatus( wave, allSuccess );
            }
            case EXECUTE_ONLY_IF_ANY_SUCCESS -> {
                boolean anySuccess = false;
                boolean depsResolved = true;

                for ( var dep : deps ) {
                    if ( wave.hasRelatedTask( dep ) ) {
                        var snapshot = wave.snapshotOfStage( dep );
                        anySuccess = anySuccess || ( snapshot.status().isFinished() && snapshot.result() == TaskResult.SUCCESS );
                    } else {
                        depsResolved = false;
                    }
                }

                if ( depsResolved && !anySuccess ) {
                    wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
                }

                if ( anySuccess && !stage.delay().isNone() ) {
                    return new StageStatus( TasksWaveDefault.<StageKind, Stage, TestLink>initWave( -1, wave.waveId() ), anySuccess );
                }

                return new StageStatus( wave, anySuccess );
            }
            case EXECUTE_ONLY_IF_ANY_FAIL -> {
                boolean anyFail = false;
                boolean depsResolved = true;

                for ( var dep : deps ) {
                    if ( wave.hasRelatedTask( dep ) ) {
                        var snapshot = wave.snapshotOfStage( dep );
                        anyFail = anyFail || ( snapshot.status().isFinished() && snapshot.result() == TaskResult.FAIL );
                    } else {
                        depsResolved = false;
                    }
                }

                if ( depsResolved && !anyFail ) {
                    wave.addSnapshot( TaskSnapshot.skipped( 0, stage.stageId() ) );
                }

                if ( anyFail && !stage.delay().isNone() ) {
                    return new StageStatus( TasksWaveDefault.<StageKind, Stage, TestLink>initWave( -1, wave.waveId() ), anyFail );
                }

                return new StageStatus( wave, anyFail );
            }
            case END -> {
                return new StageStatus( wave, true );
            }
            default ->
                throw new AssertionError( stage.kind().name() );
        }
    }

    @Override
    public TaskSnapshot activateRelatedTask(TasksWave wave, Stage stage) {
        if ( !stage.delay().isNone() ) {
            return TaskSnapshot.later( this.taskIds.get(), stage.stageId(), stage.delay() );
        }

        if ( stage instanceof ImmediateFail ) {
            return TaskSnapshot.failed( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof ImmediateSuccess ) {
            return TaskSnapshot.succeeded( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof DeferredSuccess ) {
            return TaskSnapshot.activated( this.taskIds.get(), stage.stageId() );
        } else if ( stage instanceof DeferredFail ) {
            return TaskSnapshot.activated( this.taskIds.get(), stage.stageId() );
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
    public TasksWave onEnd(TasksWave wave, TaskResult result, Stage stage) {
        throw new UnsupportedOperationException( "Not supported yet." ); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CallbacksTable<StageKind, Stage> callbacksFor(StageKind kind) {
        return this;
    }

}
