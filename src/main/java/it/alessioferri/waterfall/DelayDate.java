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
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Alessio
 */
public record DelayDate(int years, int months, int days) implements Delay {

    /**
     * Max delay possible
     */
    public static DelayDate max() {
        return new DelayDate( 99999, 0, 0 );
    }

    /**
     * None delay
     * @return a "none" delay
     */
    public static DelayDate none() {
        return new DelayDate( 0, 0, 0 );
    }

    /**
     * Delay until the specified date
     * @param to
     * @return
     */
    public static DelayDate until(LocalDate to) {
        var now = LocalDate.now();

        long days = ChronoUnit.DAYS.between( now, to );

        return new DelayDate( 0, 0, ( int ) days );
    }

    public LocalDate addTo(LocalDate date) {
        return date.plusYears( years ).plusMonths( months ).plusDays( days );
    }

    public boolean isNone() {
        return years == 0 && months == 0 && days == 0;
    }

    public Delay add(Delay b) {
        return new DelaySum(this, b);
    }

}
