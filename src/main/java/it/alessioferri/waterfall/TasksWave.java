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
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Alessio
 * @param <E>
 * @param <S>
 * @param <L>
 */
public interface TasksWave<E extends Enum, S extends FlowStage<E>, L extends Link> {

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
     * Latest snapshot of all tasks by its' stage id
     *
     * @return
     */
    public Map<Long, TaskSnapshot> snapshot();

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
     * Check if exists a related task for the speficied stage
     *
     * @param stageId id of the stage
     * @return if the stage has a related task
     */
    public default boolean hasRelatedTask(long stageId) {
        for ( var s : this.snapshot().entrySet() ) {
            var val = s.getValue();
            if ( val.stageId() == stageId && val.status() != TaskStatus.SKIPPED ) {
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

}
