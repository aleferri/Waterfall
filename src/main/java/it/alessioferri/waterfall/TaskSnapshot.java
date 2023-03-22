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

/**
 *
 * @author Alessio
 */
public record TaskSnapshot(long taskId, long stageId, LocalDate takenAt, TaskStatus status, TaskResult result) {

    public static TaskSnapshot activated(long taskId, long stageId) {
        return new TaskSnapshot( taskId, stageId, LocalDate.now(), TaskStatus.READY, TaskResult.SUCCESS );
    }

    public static TaskSnapshot later(long taskId, long stageId, Delay delay) {
        return new TaskSnapshot( taskId, stageId, delay.applyTo( LocalDate.now() ), TaskStatus.INITIALIZED, TaskResult.SUCCESS );
    }

    public static TaskSnapshot succeeded(long taskId, long stageId) {
        return new TaskSnapshot( taskId, stageId, LocalDate.now(), TaskStatus.COMPLETED, TaskResult.SUCCESS );
    }

    public static TaskSnapshot failed(long taskId, long stageId) {
        return new TaskSnapshot( taskId, stageId, LocalDate.now(), TaskStatus.COMPLETED, TaskResult.FAIL );
    }

    public static TaskSnapshot skipped(long taskId, long stageId) {
        return new TaskSnapshot( taskId, stageId, LocalDate.now(), TaskStatus.SKIPPED, TaskResult.FAIL );
    }

}
