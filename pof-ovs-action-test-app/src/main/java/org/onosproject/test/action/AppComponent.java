/*
 * Copyright 2018-present Open Networking Foundation
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
package org.onosproject.test.action;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.GroupId;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.OFPort;
import org.onosproject.floodlightpof.protocol.action.OFAction;
import org.onosproject.floodlightpof.protocol.table.OFFlowTable;
import org.onosproject.floodlightpof.protocol.table.OFTableType;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.DefaultPofActions;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.group.*;
import org.onosproject.net.table.*;
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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected GroupService groupService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationId appId;

    private byte tableId;
    private DeviceId deviceId;

    // field_id
    public final short DMAC = 0;
    public final short SMAC = 1;
    public final short TTL = 9;
    public final short SIP = 12;
    public final short DIP = 13;
    public final short TEST = 14;  // added protocol field, {272, 16, '0908'}

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.onosproject.test.action");
         pofTestStart();
//        openflowTestStart();
    }

    @Deactivate
    protected void deactivate() {
         pofTestStop();
//        openflowTestStop();
    }

    /**
     * ==================== pof test ==================
     */
    public void pofTestStart() {
        log.info("org.onosproject.pof.test.action Started");
        // deviceId = DeviceId.deviceId("pof:ffffffffcd0318d2");
        deviceId = deviceService.getAvailableDevices().iterator().next().id();
//        deviceService.changePortState(deviceId, PortNumber.portNumber(1), true);
//        deviceService.changePortState(deviceId, PortNumber.portNumber(2), true);

        // send flow table
        tableId = sendPofFlowTable(deviceId);

        // wait 1s
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* send flow rules */
//         installOutputFlowRule(deviceId, tableId, "0a000001", (int) PortNumber.CONTROLLER.toLong());
//         installSetFieldFlowRule(deviceId, tableId, "0a000001", 2);
        // installAddFieldFlowRule(deviceId, tableId, "0a000001", 2);
        // installDeleteFieldFlowRule(deviceId, tableId, "0a000001", 2);
        // installModifyFieldFlowRule(deviceId, tableId, "0a000001", 2);
//        installDropFlowRule(deviceId, tableId, "0a000001", 2);

        /* test pof_group_rule */
        install_pof_group_rule(deviceId, tableId = 0, "0a000001", 0x16, 1);
    }

    public void pofTestStop() {
        remove_pof_flow_table(deviceId, tableId);
        log.info("org.onosproject.test.action Stopped");
    }

    public byte sendPofFlowTable(DeviceId deviceId) {
        byte tableId = (byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);

        OFMatch20 srcIP = new OFMatch20();
        srcIP.setFieldId((short) SIP);
        srcIP.setFieldName("srcIP");
        srcIP.setOffset((short) 204);
        srcIP.setLength((short) 32);

        ArrayList<OFMatch20> match20List = new ArrayList<>();
        match20List.add(srcIP);

        OFFlowTable ofFlowTable = new OFFlowTable();
        ofFlowTable.setTableId(tableId);
        ofFlowTable.setTableName("FirstEntryTable");
        ofFlowTable.setMatchFieldList(match20List);
        ofFlowTable.setMatchFieldNum((byte) 1);
        ofFlowTable.setTableSize(32);
        ofFlowTable.setTableType(OFTableType.OF_MM_TABLE);
        ofFlowTable.setCommand(null);
        ofFlowTable.setKeyLength((short) 32);

        FlowTable.Builder flowTable = DefaultFlowTable.builder()
                .withFlowTable(ofFlowTable)
                .forTable(tableId)
                .forDevice(deviceId)
                .fromApp(appId);

        flowTableService.applyFlowTables(flowTable.build());

        log.info("table<{}> applied to device<{}> successfully.", tableId, deviceId.toString());

        return tableId;
    }

    public void remove_pof_flow_table(DeviceId deviceId, byte tableId) {
        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));
    }

    public void removeFlowTable(DeviceId deviceId, byte tableId) {
        // will delete flow entries first, then delete flow tables
        // flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));
        flowRuleService.removeFlowRulesById(appId);
    }

    // if outport=CONTROLLER, then it will packet_in to controller
    public void installOutputFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
//        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "00000000"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_output);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_output: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                                                   .forDevice(deviceId)
                                                   .forTable(tableId)
                                                   .withSelector(trafficSelector.build())
                                                   .withTreatment(trafficTreamt.build())
                                                   .withPriority(1)
                                                   .withCookie(newFlowEntryId)
                                                   .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installOutputFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void installSetFieldFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_set_field = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a010102", "ffffffff").action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_set_field);
        actions.add(action_output);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_set_field: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(55)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installSetFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void installAddFieldFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_add_field = DefaultPofActions.addField(TEST, (short) 272, (short) 16, "0908").action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_add_field);
        actions.add(action_output);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_add_field: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installAddFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void installDeleteFieldFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_delete_field = DefaultPofActions.deleteField((short) 272, (short) 16).action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_delete_field);
        actions.add(action_output);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_delete_field: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installDeleteFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void installModifyFieldFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // modified field
        OFMatch20 FIELD_TTL = new OFMatch20();
        FIELD_TTL.setFieldName("TTL");
        FIELD_TTL.setFieldId(TTL);
        FIELD_TTL.setOffset((short) 176);
        FIELD_TTL.setLength((short) 8);

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_delete_field = DefaultPofActions.modifyField(FIELD_TTL, 65535).action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_delete_field);
        actions.add(action_output);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_modify_field: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installModifyFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void installDropFlowRule(DeviceId deviceId, byte tableId, String srcIP, int outport) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_drop = DefaultPofActions.drop(1).action();
        actions.add(action_drop);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));
        log.info("action_drop: {}.", actions);

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(1)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installDropFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void install_pof_group_rule(DeviceId deviceId, byte tableId, String srcIP, int groupId, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_group = DefaultPofActions.group(groupId).action();
        actions.add(action_group);
        trafficTreamt.add(DefaultPofInstructions.applyActions(actions));

        // apply
        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreamt.build())
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());
    }

    /**
     * ==================== openflow test ==================
     */
    public void openflowTestStart() {
        log.info("org.onosproject.openflow.test.action Started");
        // deviceId = DeviceId.deviceId("pof:ffffffffcd0318d2");
        deviceId = deviceService.getAvailableDevices().iterator().next().id();
        deviceService.changePortState(deviceId, PortNumber.portNumber(1), true);
        deviceService.changePortState(deviceId, PortNumber.portNumber(2), true);

        /* send flow rules */
//        installControllerFlowRule(deviceId, tableId = 0);

        /* test grop table */
        installSelectGroupFlowRule(deviceId, tableId = 0);
        installGroupActionFlowRule(deviceId, tableId = 0);   // if just send group_action, then OFBAC_BAD_OUT_GROUP

        /* test mod_nw_dst */
//        install_openflow_mod_nw_dst_rule(deviceId, tableId = 0);
    }

    public void openflowTestStop() {
        /* test group table */
        removeGroupTables(deviceId);

        /* remove flow rule by appId */
        removeFlowTable(deviceId, tableId = 0);
        log.info("org.onosproject.openflow.test.action Stopped");
    }

    public void installControllerFlowRule(DeviceId deviceId, byte tableId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        // trafficSelector.matchInPort(PortNumber.portNumber(1));
        trafficSelector.matchEthType(Ethernet.TYPE_IPV4);

        // action: packet in to controller
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        PortNumber out_port = PortNumber.CONTROLLER;
        trafficTreatment.setOutput(out_port)
                        .build();

        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makePermanent()
                .build();
        flowRuleService.applyFlowRules(flowRule);
    }

    public void installGroupActionFlowRule(DeviceId deviceId, byte tableId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        trafficSelector.matchEthType(Ethernet.TYPE_IPV4)
                       .matchInPort(PortNumber.portNumber(1));

        // action: packet in to controller
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.group(new GroupId(123))
                        .build();

        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makePermanent()
                .build();
        flowRuleService.applyFlowRules(flowRule);
    }

    public void installSelectGroupFlowRule(DeviceId deviceId, byte tableId) {
        GroupId select_group_id1 = new GroupId(123);
        byte[] keyData = "abcdefg".getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);

        // bucket1: action = mod_nw_src + output
        TrafficTreatment.Builder trafficTrement_bucket1 = DefaultTrafficTreatment.builder();
        trafficTrement_bucket1.setIpSrc(IpAddress.valueOf("10.1.1.1"))
                              .setOutput(PortNumber.portNumber(2))
                              .build();
        short weight1 = 3;
        GroupBucket bucket1 = DefaultGroupBucket.createSelectGroupBucket(trafficTrement_bucket1.build(), weight1);

        // bucket2: action = mod_nw_dst + output
        TrafficTreatment.Builder trafficTrement_bucket2 = DefaultTrafficTreatment.builder();
        trafficTrement_bucket2.setIpDst(IpAddress.valueOf("10.2.2.2"))
                              .setOutput(PortNumber.portNumber(2))
                              .build();
        short weight2 = 5;
        GroupBucket bucket2 = DefaultGroupBucket.createSelectGroupBucket(trafficTrement_bucket2.build(), weight2);

        // buckets
        GroupBuckets select_group_buckets = new GroupBuckets(ImmutableList.of(bucket1, bucket2));

        // apply
        DefaultGroupDescription select_group = new DefaultGroupDescription(deviceId,
                GroupDescription.Type.SELECT, select_group_buckets, key, select_group_id1.id(), appId);
        groupService.addGroup(select_group);

    }

    public void removeGroupTables(DeviceId deviceId) {
        byte[] keyData = "abcdefg".getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);
        groupService.removeGroup(deviceId, key, appId);
    }

    public void install_openflow_mod_nw_dst_rule(DeviceId deviceId, byte tableId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        trafficSelector.matchEthType(Ethernet.TYPE_IPV4)
                        .matchInPort(PortNumber.portNumber(1));

        // action: packet in to controller
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.setIpDst(IpAddress.valueOf("10.3.3.2"))
                        .setOutput(PortNumber.portNumber(2))
                        .build();

        // apply
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(0)
                .fromApp(appId)
                .makePermanent()
                .build();
        flowRuleService.applyFlowRules(flowRule);
    }

}
