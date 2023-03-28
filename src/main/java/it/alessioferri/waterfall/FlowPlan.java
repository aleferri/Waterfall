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

import java.util.ArrayList;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alessio
 * @param <E> Enumeration of block ids
 * @param <S> FlowStage type
 * @param <L> Link type
 */
public interface FlowPlan<E extends Enum<E>, S extends FlowStage<E>, L extends Link> {

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

    /**
     * Explore the next stages in a breadth first kind
     * 
     * @param origins root set
     * @return the stages immediately after the root set
     */
    public default Collection<S> nextStagesBreadthFirst(Collection<S> origins) {
        var list = new ArrayList<S>();

        for (var origin : origins) {
            var links = this.outgoings(origin);
            for (var link : links) {
                list.add(this.followTo(link));
            }
        }

        return list;
    }

    /**
     * Breadth first exploration
     * 
     * @return a map with sequence numbers linked to the stage
     */
    public default Map<Long, Long> sequenceStages() {
        var map = new HashMap<Long, Long>();

        long s = 0;

        var iteration = this.startSet();

        while ( !iteration.isEmpty() ) {

            var toRemove = new ArrayList<S>();

            for (var i : iteration) {
                if (!map.containsKey(i.stageId())) {
                    map.put(i.stageId(), s);
                    s++;
                } else {
                    toRemove.add(i);
                }
            }

            iteration.removeAll(toRemove);

            iteration = this.nextStagesBreadthFirst(iteration);
        }

        return map;
    }

}
