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

public interface Delay {
    
    /**
     * Add to Local Date to obtain the target date
     * @param date
     * @return
     */
    public LocalDate addTo(LocalDate date);

    /**
     * Add to a delay to obtain a bigger delay
     * @param b
     * @return
     */
    public Delay add(Delay b);

    /**
     * Is less than b
     * @param b
     * @return
     */
    public default boolean lessThan(Delay b) {
        LocalDate now = LocalDate.now();
        LocalDate targetA = this.addTo(now);
        LocalDate targetB = b.addTo(now);

        return targetA.isBefore(targetB);
    }

    /**
     * Is greater than b
     * @param b
     * @return
     */
    public default boolean greaterThan(Delay b) {
        LocalDate now = LocalDate.now();
        LocalDate targetA = this.addTo(now);
        LocalDate targetB = b.addTo(now);

        return targetA.isAfter(targetB);
    }

    /**
     * Is none
     * @return
     */
    public boolean isNone();

}
