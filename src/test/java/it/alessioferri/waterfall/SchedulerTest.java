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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alessio
 */
public class SchedulerTest {

    private long nextId;

    public SchedulerTest() {
        this.nextId = 0;
    }

    @BeforeEach
    public void setUp() {
        this.nextId = 1;
    }

    public long supplyId() {
        var id = this.nextId;
        this.nextId++;

        return id;
    }

    @Test
    public void testTrivialSuccess() {
        var plan = TestPlan.empty( "TrivialSuccess" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 2 ) );

        var dispatcher = TestCallbacks.of( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        Assertions.assertEquals( 1, sched.waves().size() );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testTrivialFail() {
        var plan = TestPlan.empty( "TrivialFail" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 4, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = TestCallbacks.of( this::supplyId );;
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        Assertions.assertEquals( 1, sched.waves().size() );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testDeferredSuccess() {
        var plan = TestPlan.empty( "TrivialDeferredSuccess" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new DeferredSuccess( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = TestCallbacks.of( this::supplyId );;
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( int k = 0; k < 2; k++ ) { // in theory, only two kicks are required to finish this plan
            sched.pollSnapshotsUpdates();
            sched.updateWaves();
        }

        Assertions.assertEquals( 1, sched.waves().size() );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testDeferredFail() {
        var plan = TestPlan.empty( "TrivialDeferredFail" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new DeferredFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ANY_FAIL, DelayPolicy.SHORTEST_DELAY ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = TestCallbacks.of( this::supplyId );;
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( int k = 0; k < 2; k++ ) { // in theory, only two kicks are required to finish this plan
            sched.pollSnapshotsUpdates();
            sched.updateWaves();
        }

        Assertions.assertEquals( 1, sched.waves().size() );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testTrivialReschedule() {
        var plan = TestPlan.empty( "TrivialReschedule" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, DelayPolicy.SHORTEST_DELAY ) );
        plan.stages().add( new DeferredFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY  ) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, DelayPolicy.SHORTEST_DELAY ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 1 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 1 ), new DelayDate(0, 0, 1) );

        var dispatcher = TestCallbacks.of( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( int k = 0; k < 1; k++ ) { // in theory, only two kicks are required to finish this plan
            sched.pollSnapshotsUpdates();
            sched.updateWaves();
        }        

        Assertions.assertEquals( 2, sched.waves().size() );
        Assertions.assertFalse( sched.isComplete() );
    }

}
