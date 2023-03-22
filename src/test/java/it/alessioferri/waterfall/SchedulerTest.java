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
        var plan = TestPlan.empty( "Trivial" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 2 ) );

        var dispatcher = new TestCallbacks( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        Assertions.assertTrue( sched.waves().size() == 1 );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testTrivialFail() {
        var plan = TestPlan.empty( "Trivial" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, Delay.none() ) );
        plan.stages().add( new ImmediateFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 4, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = new TestCallbacks( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        Assertions.assertTrue( sched.waves().size() == 1 );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testDeferredSuccess() {
        var plan = TestPlan.empty( "Trivial" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, Delay.none() ) );
        plan.stages().add( new DeferredSuccess( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = new TestCallbacks( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( int k = 0; k < 2; k++ ) { // in theory, only two kicks are required to finish this plan
            for ( var w : sched.runningWaves() ) {

                var toInsert = new ArrayList<TaskSnapshot>();

                for ( var e : w.snapshot().entrySet() ) {
                    var s = e.getValue();
                    if ( s.status().isActive() ) {
                        toInsert.add( dispatcher.advanceTask( s.taskId(), plan.stageById( s.stageId() ) ) );
                    }
                }

                for ( var i : toInsert ) {
                    w.addSnapshot( i );
                }
            }

            sched.updateWaves();
        }

        Assertions.assertTrue( sched.waves().size() == 1 );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testDeferredFail() {
        var plan = TestPlan.empty( "Trivial" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, Delay.none() ) );
        plan.stages().add( new DeferredFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, Delay.none() ) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ANY_FAIL, Delay.none() ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = new TestCallbacks( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( int k = 0; k < 2; k++ ) { // in theory, only two kicks are required to finish this plan
            for ( var w : sched.runningWaves() ) {

                var toInsert = new ArrayList<TaskSnapshot>();

                for ( var e : w.snapshot().entrySet() ) {
                    var s = e.getValue();
                    if ( s.status().isActive() ) {
                        toInsert.add( dispatcher.advanceTask( s.taskId(), plan.stageById( s.stageId() ) ) );
                    }
                }

                for ( var i : toInsert ) {
                    w.addSnapshot( i );
                }
            }

            sched.updateWaves();
        }

        Assertions.assertTrue( sched.waves().size() == 1 );
        Assertions.assertTrue( sched.isComplete() );
    }

    @Test
    public void testTrivialReschedule() {
        var plan = TestPlan.empty( "Trivial" );
        plan.stages().add( new ImmediateSuccess( 1, StageKind.START, Delay.none() ) );
        plan.stages().add( new ImmediateSuccess( 2, StageKind.END, Delay.none() ) );
        plan.stages().add( new DeferredFail( 3, StageKind.EXECUTE_ONLY_IF_ALL_SUCCESS, new Delay( 1, 0, 0 ) ) );
        plan.stages().add( new DeferredSuccess( 4, StageKind.EXECUTE_ONLY_IF_ANY_FAIL, Delay.none() ) );

        plan.link( plan.stageById( 1 ), plan.stageById( 3 ) );
        plan.link( plan.stageById( 3 ), plan.stageById( 4 ) );
        plan.link( plan.stageById( 4 ), plan.stageById( 2 ) );

        var dispatcher = new TestCallbacks( this::supplyId );
        var log = LoggerFactory.getLogger( SchedulerTest.class );

        var sched = SchedulerDefault.<StageKind, Stage, TestLink>kickoff( plan, dispatcher, log );

        for ( var w : sched.runningWaves() ) {

            var toInsert = new ArrayList<TaskSnapshot>();

            for ( var e : w.snapshot().entrySet() ) {
                var s = e.getValue();
                if ( s.status().isActive() ) {
                    toInsert.add( dispatcher.advanceTask( s.taskId(), plan.stageById( s.stageId() ) ) );
                }
            }

            for ( var i : toInsert ) {
                w.addSnapshot( i );
            }
        }

        sched.updateWaves();

        Assertions.assertTrue( sched.waves().size() > 1 );
        Assertions.assertFalse( sched.isComplete() );
    }

}
