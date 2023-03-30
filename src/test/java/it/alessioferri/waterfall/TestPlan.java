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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Alessio
 */
public record TestPlan(String title, Collection<Stage> stages, Collection<TestLink> links) implements FlowPlan<StageKind, Stage, TestLink> {

    public static TestPlan empty(String title) {
        return new TestPlan( title, new ArrayList<>(), new ArrayList<>() );
    }

    public void link(Stage a, Stage b, Delay delay) {
        this.links.add( new TestLink( a.stageId(), b.stageId(), delay ) );
    }

    public void link(Stage a, Stage b) {
        this.links.add( new TestLink( a.stageId(), b.stageId(), DelayDate.none() ) );
    }

    @Override
    public Collection<Stage> startSet() {
        var list = new ArrayList<Stage>();

        for ( var stage : stages ) {
            if ( stage.kind() == StageKind.START ) {
                list.add( stage );
            }
        }

        return list;
    }

    @Override
    public Collection<TestLink> outgoings(Stage stage) {
        var list = new ArrayList<TestLink>();

        for ( var link : links ) {
            if ( link.from() == stage.stageId() ) {
                list.add( link );
            }
        }

        return list;
    }

    @Override
    public Collection<TestLink> incomings(Stage stage) {
        var list = new ArrayList<TestLink>();

        for ( var link : links ) {
            if ( link.to() == stage.stageId() ) {
                list.add( link );
            }
        }

        return list;
    }

    @Override
    public Stage stageById(long stageId) {
        for ( var stage : stages ) {
            if ( stage.stageId() == stageId ) {
                return stage;
            }
        }

        throw new RuntimeException( "this api require you to know already if a stageId is valid" );
    }

    @Override
    public Stage followTo(TestLink link) {
        return this.stageById( link.to() );
    }

    @Override
    public Stage followFrom(TestLink link) {
        return this.stageById( link.from() );
    }

}
