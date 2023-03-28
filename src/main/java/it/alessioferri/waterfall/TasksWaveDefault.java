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
 *
 * @author Alessio
 * @param <E>
 * @param <B>
 * @param <L>
 */
public record TasksWaveDefault<E extends Enum<E>, B extends FlowStage<E>, L extends Link>(
        long waveId,
        long parentWaveId,
        LocalDate startedAt,
        Map<String, Object> resources,
        Map<String, Object> scratchpad,
        Map<Long, TaskSnapshot> snapshot,
        Collection<TaskSnapshot> history,
        List<Long> cursors) implements TasksWave<E, B, L> {

    public static <E extends Enum<E>, B extends FlowStage<E>, L extends Link> TasksWaveDefault<E, B, L> initWave(long waveId, long parentWaveId) {
        System.out.println("Init wave");
        return new TasksWaveDefault<>(
                waveId,
                parentWaveId,
                LocalDate.now(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    @Override
    public List<TaskSnapshot> historyFor(long taskId) {
        var list = new ArrayList<TaskSnapshot>();

        for ( var e : this.history ) {
            if ( e.taskId() == taskId ) {
                list.add( e );
            }
        }

        return list;
    }

    @Override
    public void addSnapshot(TaskSnapshot snapshot) {
        this.history.add( snapshot );
        this.snapshot.put( snapshot.stageId(), snapshot );
    }

    @Override
    public TasksWave<E, B, L> withWaveId(long waveId) {
        return new TasksWaveDefault<>( waveId, parentWaveId, startedAt, resources, scratchpad, snapshot, history, cursors );
    }

    @Override
    public TaskSnapshot snapshotOfStage(long stageId) {
        var stageSnapshot = this.snapshot.get( stageId );

        if ( snapshot != null ) {
            return stageSnapshot;
        }

        throw new RuntimeException( "cannot find stage id, you did something wrong to the wave state or you are using the wrong API" );
    }

    @Override
    public TaskSnapshot snapshotOfTask(long taskId) {
        for ( var e : this.snapshot.entrySet() ) {
            if ( e.getValue().taskId() == taskId ) {
                return e.getValue();
            }
        }

        throw new RuntimeException( "cannot find task id, you did something wrong to the wave state or you are using the wrong API" );
    }

}
