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
import java.util.Collection;

/**
 * @author Alessio
 * @param <E> Enumeration of block ids
 * @param <S> FlowStage type
 * @param <L> Link type
 */
public interface FlowPlan<E extends Enum, S extends FlowStage<E>, L extends Link> {

    /**
     * Title of the plan
     *
     * @return
     */
    public String title();

    /**
     * FlowStage list
     *
     * @return
     */
    public Collection<S> stages();

    /**
     * Links between stages
     *
     * @return
     */
    public Collection<L> links();

    /**
     * Start set of stages for the plan
     *
     * @return
     */
    public Collection<S> startSet();

    /**
     * Fetch the outgoings links
     *
     * @param stage
     * @return
     */
    public Collection<L> outgoings(S stage);

    /**
     * Fetch the incomings links
     *
     * @param stage
     * @return
     */
    public Collection<L> incomings(S stage);

    /**
     * Return the specified block
     *
     * @param stageId
     * @return
     */
    public S stageById(long stageId);

    /**
     * Follow the link to the next block
     *
     * @param link
     * @return
     */
    public S followTo(L link);

    /**
     * Follow the link to the previous block
     *
     * @param link
     * @return
     */
    public S followFrom(L link);

}
