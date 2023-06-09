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

/**
 * @author Alessio
 * @param <E>
 * @param <S>
 * @param <L>
 */
public interface Scheduler<E extends Enum<E>, S extends FlowStage<E>, L extends Link> {

    /**
     * Started at
     *
     * @return
     */
    public LocalDate startedAt();

    /**
     * Update all the waves
     *
     * @return
     */
    public Scheduler<E, S, L> updateWaves();

    /**
     * All waves
     *
     * @return
     */
    public Collection<TasksWave<E, S, L>> waves();

    /**
     * Running waves
     *
     * @return
     */
    public Collection<TasksWave<E, S, L>> runningWaves();

    /**
     * Request this object to poll new snapshots of the active tasks
     */
    public void pollSnapshotsUpdates();

    /**
     * Is complete
     *
     * @return true if nothing need to be scheduled
     */
    public default boolean isComplete() {
        return this.runningWaves().isEmpty();
    }

}
