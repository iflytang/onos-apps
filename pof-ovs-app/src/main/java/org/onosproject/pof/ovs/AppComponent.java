/*
 * Copyright 2017-present Open Networking Laboratory
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
package org.onosproject.pof.ovs;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.action.OFAction;
import org.onosproject.floodlightpof.protocol.table.OFFlowTable;
import org.onosproject.floodlightpof.protocol.table.OFTableType;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.DefaultPofActions;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.table.DefaultFlowTable;
import org.onosproject.net.table.FlowTable;
import org.onosproject.net.table.FlowTableService;
import org.onosproject.net.table.FlowTableStore;
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
    protected FlowTableStore flowTableStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableStore tableStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableService flowTableService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.onosproject.pof.ovs");
        startPof();
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
        flowRuleService.removeFlowRulesById(appId);
    }

    /* start openflow method */
    public void startOpenFlow() {
        log.info("OpenFlow appId: {} Started", appId);
        DeviceId deviceId = deviceService.getAvailableDevices().iterator().next().id();
        log.info("deviceId: {}", deviceId);
        installOpenFlowOutputRule(deviceId.toString(), 0, "11:22:33:44:55:66", 2,1);
        installOpenFlowDropRule(deviceId.toString(), 0, "11:22:33:44:55:66", 2,1);
        log.info("installOpenFlowOutputRule successfully.");
    }

    /* start pof method */

    public void startPof() {
        log.info("Pof appId: {} Started", appId);
        DeviceId deviceId = deviceService.getAvailableDevices().iterator().next().id();
        sendPofFlowTable(deviceId);
        log.info("deviceId: {}", deviceId);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        installDropFlowRule(deviceId.toString(), 0, "112233445566", 2,1);
        log.info("installForwardFlowRule successfully.");
    }

    /* openflow rule */
    public void installOpenFlowOutputRule(String deviceId, int tableId, String srcMac, int outport, int SMAC) {

        // match: srcMac
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthSrc(MacAddress.valueOf(srcMac));

        // action: output
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.portNumber(outport)).build();

        // apply
        int randomPriority = RandomUtils.nextInt(FlowRule.MAX_PRIORITY);
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(DeviceId.deviceId(deviceId))
                .forTable(tableId)
                .withSelector(selector.build())
                .withTreatment(treatment)
                .withPriority(randomPriority)
                .fromApp(appId)
                .makePermanent()
                .build();
        flowRuleService.applyFlowRules(flowRule);
        log.info("Apply openflow output rule to OVS<%s> successfully.", deviceId);
    }

    /* openflow rule */
    public void installOpenFlowDropRule(String deviceId, int tableId, String srcMac, int outport, int SMAC) {

        // match
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthSrc(MacAddress.valueOf(srcMac));

        // action: drop
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .drop().build();

        // apply
        int randomPriority = RandomUtils.nextInt(FlowRule.MAX_PRIORITY);
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(DeviceId.deviceId(deviceId))
                .forTable(tableId)
                .withSelector(selector.build())
                .withTreatment(treatment)
                .withPriority(randomPriority)
                .fromApp(appId)
                .makePermanent()
                .build();
        flowRuleService.applyFlowRules(flowRule);
        log.info("Apply openflow drop rule to OVS<%s> successfully.", deviceId);
    }

    public byte sendPofFlowTable(DeviceId deviceId) {

        byte tableId = (byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);
        //byte smallTableId = tableStore.pashutdownNowrseToSmallTableId(deviceId, tableId);

        OFMatch20 srcIP = new OFMatch20();
        srcIP.setFieldId((short) 1);
        srcIP.setFieldName("SMAC");
        srcIP.setOffset((short) 48);
        srcIP.setLength((short) 48);

        ArrayList<OFMatch20> match20List = new ArrayList<OFMatch20>();
        match20List.add(srcIP);

        OFFlowTable ofFlowTable = new OFFlowTable();
        ofFlowTable.setTableId(tableId);
        ofFlowTable.setTableName("FirstEntryTable");
        ofFlowTable.setMatchFieldNum((byte) 1);
        ofFlowTable.setTableSize(32);
        ofFlowTable.setTableType(OFTableType.OF_MM_TABLE);
        ofFlowTable.setCommand(null);
        ofFlowTable.setKeyLength((short) 48);
        ofFlowTable.setMatchFieldList(match20List);

        FlowTable.Builder flowTable = DefaultFlowTable.builder()
                .withFlowTable(ofFlowTable)
                .forTable(tableId)
                .forDevice(deviceId)
                .fromApp(appId);

        flowTableService.applyFlowTables(flowTable.build());

        return tableId;
    }

    /* pof rule */
    public void installForwardFlowRule(String deviceId, int tableId, String srcMac, int outport, int SMAC) {

        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SMAC, (short) 48, (short) 48, srcMac, "ffffffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));
        log.info("[==installForwardFlowRule==] 1. match: {}.", matchList);

        // action
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        // OFAction action_remove_VLC = DefaultPofActions.deleteField(112, 48).action(); // VLCHeader{0, 48} is 6B in the front of IP packets
        OFAction action_outport = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        // actions.add(action_remove_VLC);
        actions.add(action_outport);
        trafficTreatment.add(DefaultPofInstructions.applyActions(actions));
        log.info("[==installForwardFlowRule==] 2. action: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(DeviceId.deviceId(deviceId), tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(DeviceId.deviceId(deviceId))
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());
        log.info("[==installForwardFlowRule==] 3. applyRuleService {} + tableId {}.",deviceId, tableId);
    }

    /* pof rule */
    public void installDropFlowRule(String deviceId, int tableId, String srcMac, int outport, int SMAC) {

        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SMAC, (short) 48, (short) 48, srcMac, "ffffffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_drop = DefaultPofActions.drop(1).action();
        // actions.add(action_remove_VLC);
        actions.add(action_drop);
        trafficTreatment.add(DefaultPofInstructions.applyActions(actions));

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(DeviceId.deviceId(deviceId), tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(DeviceId.deviceId(deviceId))
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());
    }

    /* pof rule */
    public void installGoToTableFlowRule(String deviceId, int SMAC, String srcMac, int tableId, int goToTableId) {
        // match dstIp{240b, 32b}, no VLC header here
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SMAC, (short) 48, (short) 96, srcMac, "ffffffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));
        log.info("[==installGotoTableFlowRule==] 1. match: {}.", matchList);

        // action: forward
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        trafficTreatment.add(DefaultPofInstructions.gotoTable((byte) goToTableId, (byte) 0, (byte) 0, new ArrayList<OFMatch20>()));
        log.info("[==installGotoTableFlowRule==] 2. action: {}.", actions);

        // apply flow rule to switch, globalTableId = 0 by default
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(DeviceId.deviceId(deviceId), tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(DeviceId.deviceId(deviceId))
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withCookie(newFlowEntryId)
                .withPriority(0)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());
        log.info("[==installGotoTableFlowRule==] 3. applyRuleService deviceId: {} + TableId0: {} to TableId1: {}.",
                deviceId, tableId, goToTableId);
    }

}
