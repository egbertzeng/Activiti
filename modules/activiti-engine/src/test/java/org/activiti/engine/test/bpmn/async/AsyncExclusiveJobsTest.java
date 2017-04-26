/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */
package org.activiti.engine.test.bpmn.async;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class AsyncExclusiveJobsTest extends PluggableActivitiTestCase {
	
	/** 
	 * Test for https://activiti.atlassian.net/browse/ACT-4035.
	 */
	@Deployment
	public void testExclusiveJobs() {
		
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

			processEngine.getProcessEngineConfiguration().getAsyncExecutor().setAsyncJobLockTimeInMillis(10000);
			processEngine.getProcessEngineConfiguration().getAsyncExecutor().setTimerLockTimeInMillis(10000);
			processEngine.getProcessEngineConfiguration().getAsyncExecutor().setResetExpiredJobsInterval(10000);

			// The process has two script tasks in parallel, both exclusive.
			// They should be executed with at least 6 seconds in between (as they both sleep for 6 seconds)
			runtimeService.startProcessInstanceByKey("testExclusiveJobs");
			waitForJobExecutorToProcessAllJobs(120000L, 500L);

			HistoricActivityInstance scriptTaskAInstance = historyService.createHistoricActivityInstanceQuery().activityId("scriptTaskA").singleResult();
			HistoricActivityInstance scriptTaskBInstance = historyService.createHistoricActivityInstanceQuery().activityId("scriptTaskB").singleResult();
			
			long endTimeA = scriptTaskAInstance.getEndTime().getTime();
			long endTimeB = scriptTaskBInstance.getEndTime().getTime();

			long endTimeDifference = Math.abs(endTimeB - endTimeA);

			assertTrue(endTimeDifference > 6000); // > 6000 -> jobs were executed in parallel
		}
		
	}

}
