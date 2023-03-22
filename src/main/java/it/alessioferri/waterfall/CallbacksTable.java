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

/**
 * @author Alessio
 * @param <E>
 * @param <S>
 */
public interface CallbacksTable<E extends Enum, S extends FlowStage<E>> {

    /**
     * Called when the dependencies of the stage are updated
     *
     * @param wave wave for wich the status is to be evaluated
     * @param stage stage to be evaluated
     * @param deps ids of the dependencies (only one stage materialized as task
     * per wave)
     * @return stage status for the wave
     */
    public StageStatus onDepsUpdates(TasksWave wave, S stage, List<Long> deps);

    /**
     * Activate the related task for the stage in the specified wave
     *
     * @param wave container of the new task
     * @param stage information about the task to be created
     * @return a new TaskSnapshot, the real task object is out of the this
     * library scope
     */
    public TaskSnapshot activateRelatedTask(TasksWave wave, S stage);

    /**
     * After the task has been completed
     *
     * @param wave container for the finished task
     * @param result result of the task (either success or fail)
     * @param stage origin of the task
     * @return a new TaskWave if the stage was to be rescheduled at a later time
     * or the same wave otherwise
     */
    public TasksWave onEnd(TasksWave wave, TaskResult result, S stage);

}
