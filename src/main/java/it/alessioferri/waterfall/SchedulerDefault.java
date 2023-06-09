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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 *
 * @author Alessio
 */
public record SchedulerDefault<E extends Enum<E>, S extends FlowStage<E>, L extends Link>(FlowPlan<E, S, L> plan,
        Map<Long, Long> sequence, List<TasksWave<E, S, L>> waves, Dispatcher<E, S, L> dispatcher, Logger log)
        implements Scheduler<E, S, L> {

    /**
     * Kickoff a new SchedulerDefault
     *
     * @param <E>        Enum type
     * @param <S>        FlowStage with E as its' kind
     * @param <L>        Link
     * @param plan       plan to be followed
     * @param dispatcher dispatcher for callbacks
     * @param log        logger for the debug information
     * @return a scheduler for the plan
     */
    public static <E extends Enum<E>, S extends FlowStage<E>, L extends Link> SchedulerDefault<E, S, L> kickoff(
            FlowPlan<E, S, L> plan, Dispatcher<E, S, L> dispatcher, Logger log) {
        var scheduler = new SchedulerDefault<E, S, L>( plan, plan.sequenceStages(), new ArrayList<>(), dispatcher,
                log );
        var entry = scheduler.kickoffWave( 0, -1, DelayDate.none(), plan.startSet() );
        scheduler.waves.add( entry );
        scheduler.updateWaves();

        return scheduler;
    }

    @Override
    public LocalDate startedAt() {
        if ( waves.isEmpty() ) {
            throw new RuntimeException( "The plan has not started" );
        }
        return waves.get( 0 ).startedAt();
    }

    private boolean isBackwardLink(Link link) {
        var start = this.sequence.get( link.from() );
        var end = this.sequence.get( link.to() );

        return start >= end;
    }

    /**
     * Mark the reachable set of stages from the starting set
     *
     * @param reachableSet current reachable set
     * @param cursor       forward cursor to explore the graph
     */
    private void markReachableSet(Collection<S> reachableSet, S cursor) {
        var outgoings = this.plan.outgoings( cursor );

        for ( var outgoing : outgoings ) {
            var follow = this.plan.followTo( outgoing );

            if ( !reachableSet.contains( follow ) ) {
                reachableSet.add( follow );
                this.markReachableSet( reachableSet, follow );
            }
        }
    }

    /**
     * Kickoff a new wave
     *
     * @param waveId   initial wave id
     * @param parentId parent wave id
     * @param startSet start set of stages
     * @return a new TasksWave with the speficied parameter
     */
    private TasksWaveDefault<E, S, L> kickoffWave(long waveId, long parentId, Delay waveDelay, Collection<S> startSet) {
        var wave = TasksWaveDefault.<E, S, L>initWave( waveId, parentId );

        var reachableSet = new HashSet<S>();
        reachableSet.addAll( startSet );

        for ( var e : startSet ) {
            System.out.println( e );
            log.debug( "Processing: " + e.stageId() );

            this.markReachableSet( reachableSet, e );

            var snapshot = this.dispatcher.schedulerFor( e.kind() ).scheduleTaskFor( wave, e, Collections.emptyList(),
                    waveDelay );
            wave.addSnapshot( snapshot );
            wave.cursors().add( e.stageId() );

            log.debug( "Initial snapshot for planned task " + e.stageId() + " is: " + snapshot.toString() );
        }

        for ( var s : this.plan.stages() ) {
            if ( !reachableSet.contains( s ) ) {
                wave.addSnapshot( TaskSnapshot.skipped( 0, s.stageId() ) );
            }
        }

        return wave;
    }

    private void updateWave(TasksWave<E, S, L> t) {
        for ( int i = 0; i < t.cursors().size(); ) {

            var cursor = t.cursors().get( i );

            log.debug( "Processing cursor: " + cursor );

            var stage = this.plan.stageById( cursor );

            if ( !t.hasRelatedTask( stage ) ) {
                log.debug( "Missing related task for stage id: " + stage.stageId() );
                t.cursors().remove( i );
                continue;
            }

            var taskSnapshot = t.snapshotOfStage( stage.stageId() );

            System.out.println(
                    this.plan.title() + " cursor: " + cursor + ", Task status is: " + taskSnapshot.status().name() );
            log.debug( "Task status is: " + taskSnapshot.status().name() );

            if ( taskSnapshot.status().isFinished() ) {

                var links = this.plan.outgoings( stage );
                System.out.println( "Outgoings: " + links );

                for ( var link : links ) {

                    var next = this.plan.followTo( link );
                    var incomings = this.plan.incomings( next );
                    var deps = new ArrayList<Long>();

                    for ( var incoming : incomings ) {
                        deps.add( incoming.from() );
                    }

                    var callbacks = this.dispatcher.schedulerFor( next.kind() );

                    if ( this.isBackwardLink( link ) ) {
                        var maybeNewWave = callbacks.onBackwardLinkUpdate( t, next, incomings, link.from() );

                        if ( maybeNewWave.isPresent() ) {
                            var data = maybeNewWave.get();
                            this.waves.add( this.kickoffWave( this.waves.size(), data.parentWave(), data.waveDelay(),
                                    data.startSet() ) );
                        }

                    } else {
                        var result = callbacks.onDepsUpdates( t, next, deps );

                        if ( result.canActivate() ) {
                            var snapshot = callbacks.scheduleTaskFor( t, next, incomings, DelayDate.none() );
                            t.addSnapshot( snapshot );
                            t.cursors().add( next.stageId() );
                        }

                        if ( result.maybeNew().waveId() == -1 ) {
                            this.waves.add( result.maybeNew().withWaveId( this.waves.size() ) );
                        }
                    }

                }

                t.cursors().remove( i );

                log.debug( "Removing cursor as the task is finished" );
            } else {
                i++;
            }

            log.debug( "Ready for next cursor at index: " + i );
        }

        if ( t.cursors().isEmpty() ) {
            log.debug( "Current wave is complete, skipping all the remaining stages" );

            for ( var s : plan.stages() ) {
                if ( !t.hasRelatedTask( s ) ) {
                    t.addSnapshot( TaskSnapshot.skipped( 0, s.stageId() ) );
                }
            }
        }
    }

    @Override
    public Scheduler<E, S, L> updateWaves() {
        for ( int i = 0; i < this.waves.size(); i++ ) {
            var t = this.waves.get( i );
            if ( t.hasUnresolvedTasks() ) {
                this.updateWave( t );
            }
        }

        return this;
    }

    @Override
    public Collection<TasksWave<E, S, L>> runningWaves() {
        var list = new ArrayList<TasksWave<E, S, L>>();

        for ( var e : this.waves ) {
            if ( e.hasUnresolvedTasks() ) {
                list.add( e );
            }
        }

        return list;
    }

    @Override
    public void pollSnapshotsUpdates() {
        for ( var w : this.waves ) {
            var latest = w.latestSnapshotByTask();

            for ( var t : latest.keySet() ) {
                var s = latest.get( t );
                var n = this.dispatcher.takeSnapshot( w, this.plan.stageById( s.stageId() ), t );
                if ( n.status() != s.status() ) {
                    w.addSnapshot( n );
                }
            }
        }
    }

}
