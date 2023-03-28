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
public record Delay(int years, int months, int days) {

    public static Delay max() {
        return new Delay( 99999, 0, 0 );
    }

    public static Delay none() {
        return new Delay( 0, 0, 0 );
    }

    public static Delay until(LocalDate to) {
        var now = LocalDate.now();

        long days = ChronoUnit.DAYS.between( now, to );

        return new Delay( 0, 0, ( int ) days );
    }

    public LocalDate applyTo(LocalDate date) {
        return date.plusYears( years ).plusMonths( months ).plusDays( days );
    }

    public boolean lessThan(Delay b) {
        if ( years < b.years ) {
            return true;
        }
        if ( months < b.months ) {
            return true;
        }
        return days < b.days;
    }

    public boolean greaterThan(Delay b) {
        if ( years > b.years ) {
            return true;
        }

        if ( months > b.months ) {
            return true;
        }

        return days > b.days;
    }

    public boolean isNone() {
        return years == 0 && months == 0 && days == 0;
    }

}
