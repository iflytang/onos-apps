/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author iflytang
 * @date 17-12-06
 * @desc send flow rule to openflow switch.
 */
package org.onosproject.openflow.ovs;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.instruction.OFInstruction;
import org.onosproject.floodlightpof.protocol.instruction.OFInstructionGotoTable;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.flow.instructions.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.onosproject.openflow.ovs");
        log.info("appId: {} Started", appId);
        DeviceId deviceId = deviceService.getAvailableDevices().iterator().next().id();
        log.info("deviceId: {}", deviceId);
        installFlowRule(deviceId);
        installGotoTableFlowRule(deviceId);

    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
        flowRuleService.removeFlowRulesById(appId);
    }

    public void installGotoTableFlowRule(DeviceId deviceId) {
        // treatment
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.transition(1);  // go to table

        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(0)
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makeTemporary(90)
                .build();
        flowRuleService.applyFlowRules(flowRule);
    }

    public void installFlowRule(DeviceId deviceId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        trafficSelector.matchEthSrc(MacAddress.valueOf("01:02:03:04:05:06"))
                .matchEthDst(MacAddress.valueOf("11:22:33:44:55:66"));

        // action
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.setEthSrc(MacAddress.valueOf("ff:ff:ff:ff:ff:ff"))
                        .setOutput(PortNumber.portNumber(2))
                        .build();
        
        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(1)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makeTemporary(90)
                .build();
        flowRuleService.applyFlowRules(flowRule);

    }

    public void installP0FlowRule(DeviceId deviceId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        trafficSelector.matchInPort(PortNumber.portNumber(1));

        // action
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.setOutput(PortNumber.portNumber(2))
                .build();

        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(1)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makeTemporary(90)
                .build();
        flowRuleService.applyFlowRules(flowRule);

    }
}
