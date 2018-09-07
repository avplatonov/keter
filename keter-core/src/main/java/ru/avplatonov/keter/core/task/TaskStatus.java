/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.avplatonov.keter.core.task;

/** */
public enum TaskStatus {
    /** Task awaits other tasks on witch it depends. */
    AWAITING_DEPENDENCIES,
    /** Task awaits resources for running it. */
    PENDING,
    /** Task was locked by worker but not started. */
    LOCKED,
    /** Task was started for execution on some worker. */
    RUNNING,
    /** Task was completed by worker. */
    COMPLETED,
    /** Task was completed with error. Comment for task should contains causes of fail. */
    FAILED
}
