/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.messages.checkpoint;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.checkpoint.CheckpointException;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.util.SerializedThrowable;

/**
 * This message is sent from the {@link org.apache.flink.runtime.taskexecutor.TaskExecutor} to the
 * {@link org.apache.flink.runtime.jobmaster.JobMaster} to tell the checkpoint coordinator
 * that a checkpoint request could not be heeded. This can happen if a Task is already in
 * RUNNING state but is internally not yet ready to perform checkpoints.
 */
public class DeclineCheckpoint extends AbstractCheckpointMessage implements java.io.Serializable {

	private static final long serialVersionUID = 2094094662279578953L;

	/** The reason why the checkpoint was declined. */
	private final Throwable reason;

	public DeclineCheckpoint(JobID job, ExecutionAttemptID taskExecutionId, long checkpointId) {
		this(job, taskExecutionId, checkpointId, null);
	}

	public DeclineCheckpoint(JobID job, ExecutionAttemptID taskExecutionId, long checkpointId, Throwable reason) {
		super(job, taskExecutionId, checkpointId);

		if (reason == null) {
			this.reason = reason;
		} else {
			// Replace with a serialized throwable in case the cause (or root cause) is not in the default classloader. 
			//
			// An exception that is in the default classloader will fail to deserialize if its root cause
			// is not in the default classloader, e.g. a CheckpointException (on the classpath) with root cause
			// KafkaException (not on the classpath). See: https://issues.apache.org/jira/browse/FLINK-14076
			this.reason = new SerializedThrowable(reason);
		}
	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Gets the reason why the checkpoint was declined.
	 *
	 * @return The reason why the checkpoint was declined
	 */
	public Throwable getReason() {
		return reason;
	}

	// --------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		return String.format("Declined Checkpoint %d for (%s/%s): %s",
				getCheckpointId(), getJob(), getTaskExecutionId(), reason);
	}
}
