/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
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
 */
package org.jahia.pipelines.valves;

import org.jahia.pipelines.PipelineException;
import org.jahia.operations.PageState;

/**
 * @author <a href="mailto:david@bluesunrise.com">David Sean Taylor</a>
 * @version $Id$
 */
public interface ValveContext {
    final ThreadLocal<PageState> valveResources = new ThreadLocal<PageState>();
    /**
     * <p>Cause the <code>invoke()</code> method of the next Valve
     * that is part of the Pipeline currently being processed (if any)
     * to be executed, passing on the specified request and response
     * objects plus this <code>ValveContext</code> instance.
     * Exceptions thrown by a subsequently executed Valve will be
     * passed on to our caller.</p>
     *
     * <p>If there are no more Valves to be executed, execution of
     * this method will result in a no op.</p>
     *
     * @param context The run-time information, including the servlet
     * request and response we are processing.
     *
     * @exception PipelineException Thrown by a subsequent Valve.
     */
    public void invokeNext(Object context)
        throws PipelineException;
}
