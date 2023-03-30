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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alessio
 * @param <E>
 * @param <S>
 * @param <L>
 */
public interface TasksWave<E extends Enum<E>, S extends FlowStage<E>, L extends Link> {

    /**
     * Parent Thread id
     *
     * @return
     */
    public long parentWaveId();

    /**
     * Thread Id
     *
     * @return
     */
    public long waveId();

    /**
     * Started At
     *
     * @return
     */
    public LocalDate startedAt();

    /**
     * Resources of the thread
     *
     * @return
     */
    public Map<String, Object> resources();

    /**
     * Scratchpad for the data
     *
     * @return
     */
    public Map<String, Object> scratchpad();

    /**
     * Cursors for the execution
     *
     * @return
     */
    public List<Long> cursors();

    /**
     * History of all snapshot
     *
     * @return
     */
    public Collection<TaskSnapshot> history();

    /**
     * History for a specific snapshot
     *
     * @param taskId id of the specified task
     * @return a sequence of the snapshots for the specified task
     */
    public List<TaskSnapshot> historyFor(long taskId);

    /**
     * Find latest snapshot by it's stage id
     *
     * @param stageId identifier of the stage
     * @return latest snapshot of the related task
     */
    public TaskSnapshot snapshotOfStage(long stageId);

    /**
     * Find latest snapshot by it's task id
     *
     * @param taskId task identifier
     * @return latest snapshot of the task
     */
    public TaskSnapshot snapshotOfTask(long taskId);

    /**
     * Latest snapshot by stage id
     * 
     * @return
     */
    public default HashMap<Long, TaskSnapshot> latestSnapshotByStage() {
        var map = new HashMap<Long, TaskSnapshot>();

        for ( var e : this.history() ) {
            map.put( e.stageId(), e );
        }

        return map;
    }

    /**
     * Latest snapshot by task id
     * 
     * @return
     */
    public default HashMap<Long, TaskSnapshot> latestSnapshotByTask() {
        var map = new HashMap<Long, TaskSnapshot>();

        for ( var e : this.history() ) {
            map.put( e.taskId(), e );
        }

        return map;
    }

    /**
     * Check if exists a related task for the speficied stage
     *
     * @param stageId id of the stage
     * @return if the stage has a related task
     */
    public default boolean hasRelatedTask(long stageId) {
        for ( var s : this.history() ) {
            if ( s.stageId() == stageId && s.status() != TaskStatus.SKIPPED ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if exists a related task for the speficied stage
     *
     * @param stage stage to be checked
     * @return if the stage has a related task
     */
    public default boolean hasRelatedTask(S stage) {
        return this.hasRelatedTask( stage.stageId() );
    }

    /**
     * Add a snapshot for a task
     *
     * @param snapshot
     */
    public void addSnapshot(TaskSnapshot snapshot);

    /**
     * Check if there is something to resolve
     *
     * @return
     */
    public default boolean hasUnresolvedTasks() {
        return !this.cursors().isEmpty();
    }

    /**
     * Create a shallow copy with the specified id
     *
     * @param waveId new id of the wave
     * @return a shallow copy with a different id
     */
    public TasksWave<E, S, L> withWaveId(long waveId);

    public default Delay selectDelayFor(S s, DelayPolicy policy, Collection<L> incomings, TaskResult target) {
        if ( incomings.isEmpty() ) {
            return DelayDate.none();
        }

        Delay delay = policy == DelayPolicy.SHORTEST_DELAY ? DelayDate.max() : DelayDate.none();

        for ( var i : incomings ) {

            var status = this.snapshotOfStage( i.from() );

            if ( !status.status().isFinished() || status.result() != target ) {
                continue;
            }

            if ( policy == DelayPolicy.SHORTEST_DELAY ) {
                if ( i.delay().lessThan( delay ) ) {
                    delay = i.delay();
                }
            } else {
                if ( i.delay().greaterThan( delay ) ) {
                    delay = i.delay();
                }
            }
        }

        return delay;
    }

    public default DependenciesInfo queryDependenciesInfo(Collection<Long> stagesId) {
        var list = new ArrayList<TaskSnapshot>();

        for ( var id : stagesId ) {
            if ( this.hasRelatedTask( id ) ) {
                list.add( this.snapshotOfStage( id ) );
            } else {
                list.add( TaskSnapshot.queued( id ) );
            }
        }

        return new DependenciesInfo( list );
    }

}
