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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private byte tableId;              // used for openflow
    private DeviceId deviceId;

    private byte global_table_id_1;    // used for pof
    private byte global_table_id_2;    // used for pof

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
        byte tableId = sendPofFlowTable(deviceId, "FirstEntryTable");
        byte next_table_id = sendPofFlowTable(deviceId, "AddVlcHeaderTable");

        log.info("pof-ovs-action-test-app: deviceId={}, tableId={}", deviceId, tableId);
        log.info("pof-ovs-action-test-app: next_table_id={}", next_table_id);

        // used for removing flow_table in pofTestStop()
        global_table_id_1 = tableId;
        global_table_id_2 = next_table_id;

        // wait 1s
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* send flow rules */

        /* test pof_output flow rule */
//        install_pof_output_flow_rule(deviceId, tableId, "0a000001", 2, 12);

//         install_pof_set_field_rule(deviceId, tableId, "0a000001", 2, 1);
//         installDeleteFieldFlowRule(deviceId, tableId, "0a000001", 2);
        // installModifyFieldFlowRule(deviceId, tableId, "0a000001", 2);
//        installDropFlowRule(deviceId, tableId, "0a000001", 2);

        /* test pof_select_group_rule */
//        install_pof_select_group_rule(deviceId, tableId, "0a000001", "abc", 0x16, 1, true, "def");
//        install_pof_group_rule(deviceId, tableId, "0a000001", 0x16, 1);

        /* test pof_all_group_rule */
//        install_pof_all_group_rule(deviceId, tableId, "0a000001", "abc", 0x16, 1, true, "def");
//        install_pof_group_rule(deviceId, tableId, "0a000001", 0x16, 1);

//        try {
//            Thread.sleep(3000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /* test mod pof_group_rule */
//        install_pof_select_group_rule(deviceId, tableId, "0a000001", "abc", 0x16, 1, false, "def");
//        install_pof_group_rule(deviceId, tableId, "0a000001", 0x33, 1);

        /* test write_metadata and add_vlc_header */
        install_pof_write_metadata_from_packet_entry(deviceId, tableId, next_table_id, "0a000001", 12);
        install_pof_add_vlc_header_entry(deviceId, next_table_id, "0a000001", 2, 1,
                (byte) 0x01, (short) 0x0002, (short) 0x0003, (short) 0x0004);
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // delete old entry before send new entry
        install_pof_add_vlc_header_entry(deviceId, next_table_id, "0a000001", 2, 1,
                (byte) 0x04, (short) 0x0003, (short) 0x0002, (short) 0x0001);

        /* test add static field flow rule */
//        install_pof_add_static_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test add dynamic INT field flow rule */
//        install_pof_add_dynamic_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test set_field flow rule (set_dst_ip) */
//        install_pof_set_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test modify_field flow rule */
//        install_pof_modify_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test delete_field flow rule */
//        install_pof_delete_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test delete_trunc_field flow rule */
//        install_pof_delete_trunc_field_rule(deviceId, tableId, "0a000001", 2, 12);

        /* test delete_int_field flow rule */
//        install_pof_delete_int_field_rule(deviceId, tableId, "0a000001", 2, 12);
    }

    public void pofTestStop() {
        /* remove group tables */
        remove_pof_group_tables(deviceId, "abc");
//        remove_pof_group_tables(deviceId, "def");

        remove_pof_flow_table(deviceId, global_table_id_1);
        remove_pof_flow_table(deviceId, global_table_id_2);
        log.info("org.onosproject.test.action Stopped");
    }

    public byte sendPofFlowTable(DeviceId deviceId, String table_name) {
        byte tableId = (byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);

        OFMatch20 srcIP = new OFMatch20();
        srcIP.setFieldId((short) SIP);
        srcIP.setFieldName("srcIP");
        srcIP.setOffset((short) 208);
        srcIP.setLength((short) 32);

        ArrayList<OFMatch20> match20List = new ArrayList<>();
        match20List.add(srcIP);

        OFFlowTable ofFlowTable = new OFFlowTable();
        ofFlowTable.setTableId(tableId);
        ofFlowTable.setTableName(table_name);
        ofFlowTable.setMatchFieldList(match20List);
        ofFlowTable.setMatchFieldNum((byte) 1);
        ofFlowTable.setTableSize(2);
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
        flowRuleService.removeFlowRulesById(appId);  // for ovs-pof
        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));
    }

    public void removeFlowTable(DeviceId deviceId, byte tableId) {
        // will delete flow entries first, then delete flow tables
        // flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(tableId));
        flowRuleService.removeFlowRulesById(appId);
    }

    // if outport=CONTROLLER, then it will packet_in to controller
    public void install_pof_output_flow_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
//        matchList.add(Criteria.matchOffsetLength((short) SIP, (short) 208, (short) 32, srcIP, "00000000"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
//        OFAction action_add_field1 = DefaultPofActions.addField((short) 16, (short) 272, (short) 64, "0102030405060708").action();
//        actions.add(action_add_field1);
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
                                                   .withPriority(priority)
                                                   .withCookie(newFlowEntryId)
                                                   .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installOutputFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void install_pof_set_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_set_dstIp = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a020202", "ffffffff").action();
//        OFAction action_set_srcIp = DefaultPofActions.setField(SIP, (short) 208, (short) 32, "0a0a0a0a", "ffffffff").action();
//        OFAction action_set_ttl = DefaultPofActions.setField(TTL, (short) 176, (short) 8, "66", "ff").action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_set_dstIp);
//        actions.add(action_set_srcIp);
//        actions.add(action_set_ttl);
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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installSetFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void install_pof_add_static_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        short field_id1 = 17;
        short field_id2 = 18;
        short field_id3 = 19;
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_add_field1 = DefaultPofActions.addField(field_id1, (short) 272, (short) 16, "0908").action();
//        OFAction action_add_field2 = DefaultPofActions.addField(field_id2, (short) 272, (short) 16, "1918").action();
//        OFAction action_add_field3 = DefaultPofActions.addField(field_id3, (short) 272, (short) 16, "2928").action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_add_field1);
//        actions.add(action_add_field2);
//        actions.add(action_add_field3);
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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installAddFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }


    public void install_pof_add_dynamic_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        short field_id1 = -1;
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();

        // 0b'00 00 00 00 = x | x | bandwidth | egress_time || ingress_time | out_port | in_port | dpid.
        OFAction action_add_field1 = DefaultPofActions.addField(field_id1, (short) 272, (short) 16, "18").action();

        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_add_field1);

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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("install_pof_dynamic_field_flow_rule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    public void install_pof_delete_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        short field_id1 = 17;
        short offset = 272;
        int len = 16;
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_delete_field = DefaultPofActions.deleteField(offset, len).action();
//        OFAction action_delete_field1 = DefaultPofActions.deleteField((short) 272, (short) 16).action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        actions.add(action_delete_field);
//        actions.add(action_delete_field1);
//        actions.add(action_delete_field1);
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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installDeleteFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    /* if 'offset' = -1, then truncate packets into 'len' length from pkt_header. */
    public void install_pof_delete_trunc_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        short offset = -1;
        int len = 8 * 8;
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_delete_field = DefaultPofActions.deleteField(offset, len).action();
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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installDeleteFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }

    /* if 'len' = -1, then delete INT data according to its 'mapInfo', 'offset' defines the start location of INT_header */
    public void install_pof_delete_int_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action
        short offset = 34 * 8;
        int len = -1;
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_delete_field = DefaultPofActions.deleteField(offset, len).action();
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
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());

        log.info("installDeleteFieldFlowRule: apply to deviceId<{}> tableId<{}>", deviceId.toString(), tableId);
    }


    public void install_pof_modify_field_rule(DeviceId deviceId, byte tableId, String srcIP, int outport, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // modify ttl
        OFMatch20 FIELD_TTL = new OFMatch20();
        FIELD_TTL.setFieldName("TTL");
        FIELD_TTL.setFieldId(TTL);
        FIELD_TTL.setOffset((short) 176);
        FIELD_TTL.setLength((short) 8);

        // modify srcIp's last byte
        OFMatch20 FIELD_SIP = new OFMatch20();
        FIELD_SIP.setFieldName("SIP");
        FIELD_SIP.setFieldId(SIP);
        FIELD_SIP.setOffset((short) (208 + 24));
        FIELD_SIP.setLength((short) 8);

        // modify dstIp's last byte
        OFMatch20 FIELD_DIP = new OFMatch20();
        FIELD_DIP.setFieldName("DIP");
        FIELD_DIP.setFieldId(DIP);
        FIELD_DIP.setOffset((short) (240 + 24));
        FIELD_DIP.setLength((short) 8);

        // action
        TrafficTreatment.Builder trafficTreamt = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_modify_ttl = DefaultPofActions.modifyField(FIELD_TTL, 65535).action();
//        OFAction action_modify_dip = DefaultPofActions.modifyField(FIELD_DIP, 12).action();
//        OFAction action_modify_sip = DefaultPofActions.modifyField(FIELD_SIP, 12).action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport).action();
        OFAction action_add_field1 = DefaultPofActions.addField((short) 16, (short) 272, (short) 64, "0102030405060708").action();
        actions.add(action_add_field1);
        actions.add(action_modify_ttl);
//        actions.add(action_modify_dip);
//        actions.add(action_modify_sip);
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
                .withPriority(priority)
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

    public void install_pof_select_group_rule(DeviceId deviceId, byte tableId, String srcIP,String old_key_str, int groupId,
                                              int priority, boolean is_add, String new_key_str) {
        GroupId select_group_id = new GroupId(groupId);

        byte[] keyData = old_key_str.getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);

        // out_port
        int port1 = 1;
        int port2 = 2;
        int controller_port = (int) PortNumber.CONTROLLER.toLong();
        int port = port2;

        // bucket1: action
        TrafficTreatment.Builder trafficTreatment_bucket1 = DefaultTrafficTreatment.builder();
        List<OFAction> actions_bucket1 = new ArrayList<>();
        OFAction action_set_dstIp1 = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a010102", "ffffffff").action();
        OFAction action_output1 = DefaultPofActions.output((short) 0, (short) 0, (short) 0, controller_port).action();
        actions_bucket1.add(action_set_dstIp1);
        actions_bucket1.add(action_output1);
        trafficTreatment_bucket1.add(DefaultPofInstructions.applyActions(actions_bucket1));

        // bucket1: weight
        short weight1 = 2;
        GroupBucket bucket1 = DefaultGroupBucket.createSelectGroupBucket(trafficTreatment_bucket1.build(), weight1);

        // bucket2: action
        TrafficTreatment.Builder trafficTreatment_bucket2 = DefaultTrafficTreatment.builder();
        List<OFAction> actions_bucket2 = new ArrayList<>();
        OFAction action_set_dstIp2 = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a020202", "ffffffff").action();
        OFAction action_add_field1 = DefaultPofActions.addField(TEST, (short) 272, (short) 16, "0908").action();
        OFAction action_output2 = DefaultPofActions.output((short) 0, (short) 0, (short) 0, port).action();
        actions_bucket2.add(action_set_dstIp2);
        actions_bucket2.add(action_add_field1);
        actions_bucket2.add(action_output2);
        trafficTreatment_bucket2.add(DefaultPofInstructions.applyActions(actions_bucket2));

        // bucket2: weight
        short weight2 = 3;
        GroupBucket bucket2 = DefaultGroupBucket.createSelectGroupBucket(trafficTreatment_bucket2.build(), weight2);

        // buckets
        GroupBuckets select_group_buckets = new GroupBuckets(ImmutableList.of(bucket1, bucket2));
//        GroupBuckets select_group_buckets = new GroupBuckets(ImmutableList.of(bucket1));

        // apply
        DefaultGroupDescription select_group = new DefaultGroupDescription(deviceId,
                GroupDescription.Type.SELECT, select_group_buckets, key, select_group_id.id(), appId);

        if (is_add) {  // add group
            log.info("Add group table");
            groupService.addGroup(select_group);
        } else {      // mod group
            log.info("Mod group table");
            byte[] new_keyData = new_key_str.getBytes();
            final GroupKey new_key = new DefaultGroupKey(new_keyData);
            GroupBuckets new_buckets = new GroupBuckets(ImmutableList.of(bucket2));
            groupService.setBucketsForGroup(deviceId, key, new_buckets, new_key, appId);
        }

    }

    public void install_pof_all_group_rule(DeviceId deviceId, byte tableId, String srcIP,String old_key_str, int groupId,
                                              int priority, boolean is_add, String new_key_str) {
        GroupId select_group_id = new GroupId(groupId);

        byte[] keyData = old_key_str.getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);

        // out_port
        int port1 = 1;
        int port2 = 2;
        int controller_port = (int) PortNumber.CONTROLLER.toLong();
        int port = port2;

        // bucket1: controller_action will steal packets, so it should run as the last bucket.
        short offset = -1;
        int len = 14 * 8;
        TrafficTreatment.Builder trafficTreatment_bucket1 = DefaultTrafficTreatment.builder();
        List<OFAction> actions_bucket1 = new ArrayList<>();
        OFAction action_set_dstIp1 = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a010102", "ffffffff").action();
//        OFAction action_delete_trunc_field = DefaultPofActions.deleteField(offset, len).action();
        OFAction action_output1 = DefaultPofActions.output((short) 0, (short) 0, (short) 0, controller_port).action();
        actions_bucket1.add(action_set_dstIp1);
//        actions_bucket1.add(action_delete_trunc_field);
        actions_bucket1.add(action_output1);
        trafficTreatment_bucket1.add(DefaultPofInstructions.applyActions(actions_bucket1));

        // bucket1: weight
        short weight1 = 3;
        GroupBucket bucket1 = DefaultGroupBucket.createAllGroupBucket(trafficTreatment_bucket1.build());

        // bucket2: action
        TrafficTreatment.Builder trafficTreatment_bucket2 = DefaultTrafficTreatment.builder();
        List<OFAction> actions_bucket2 = new ArrayList<>();
        OFAction action_set_dstIp2 = DefaultPofActions.setField(DIP, (short) 240, (short) 32, "0a020202", "ffffffff").action();
//        OFAction action_add_field1 = DefaultPofActions.addField(TEST, (short) 272, (short) 16, "0908").action();
        OFAction action_delete_field = DefaultPofActions.deleteField((short) 272, 16).action();
        OFAction action_output2 = DefaultPofActions.output((short) 0, (short) 0, (short) 0, port2).action();
        actions_bucket2.add(action_set_dstIp2);
        actions_bucket2.add(action_delete_field);
//        actions_bucket2.add(action_add_field1);
        actions_bucket2.add(action_output2);
        trafficTreatment_bucket2.add(DefaultPofInstructions.applyActions(actions_bucket2));

        // bucket2: weight
        short weight2 = 5;
        GroupBucket bucket2 = DefaultGroupBucket.createAllGroupBucket(trafficTreatment_bucket2.build());

        // buckets: controller_action will steal packets, so it should run as the last bucket.
        GroupBuckets all_group_buckets = new GroupBuckets(ImmutableList.of(bucket1, bucket2));
//        GroupBuckets select_group_buckets = new GroupBuckets(ImmutableList.of(bucket1));

        // apply
        DefaultGroupDescription all_group = new DefaultGroupDescription(deviceId,
                GroupDescription.Type.ALL, all_group_buckets, key, select_group_id.id(), appId);

        if (is_add) {  // add group
            log.info("Add group table");
            groupService.addGroup(all_group);
        } else {      // mod group
            log.info("Mod group table");
            byte[] new_keyData = new_key_str.getBytes();
            final GroupKey new_key = new DefaultGroupKey(new_keyData);
            GroupBuckets new_buckets = new GroupBuckets(ImmutableList.of(bucket2));
            groupService.setBucketsForGroup(deviceId, key, new_buckets, new_key, appId);
        }

    }

    public void remove_pof_group_tables(DeviceId deviceId, String key_str) {
        byte[] keyData = key_str.getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);
        groupService.removeGroup(deviceId, key, appId);
    }

    public void install_pof_write_metadata_from_packet_entry(DeviceId deviceId, int tableId, int next_table_id,
                                                             String srcIP, int priority) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // metadata bits
        short metadata_offset = 32;
        short udp_len_offset = 304;    // the offset of `len` field in udp
        short write_len = 16;          // the length of `len` field in udp

        // next_table_match_field (should same as next_table), here is still srcIP
        OFMatch20 next_table_match_srcIP = new OFMatch20();
        next_table_match_srcIP.setFieldId(SIP);
        next_table_match_srcIP.setFieldName("srcIP");
        next_table_match_srcIP.setOffset((short) 208);
        next_table_match_srcIP.setLength((short) 32);

        ArrayList<OFMatch20> match20List = new ArrayList<>();
        match20List.add(next_table_match_srcIP);

        byte next_table_match_field_num = 1;
        short next_table_packet_offset = 0;

        // instruction
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.add(DefaultPofInstructions
                .writeMetadataFromPacket(metadata_offset, udp_len_offset, write_len));
        trafficTreatment.add(DefaultPofInstructions
                .gotoTable((byte) next_table_id, next_table_match_field_num, next_table_packet_offset, match20List));
//                .gotoDirectTable((byte) next_table_id, (byte) 0, (short) 0, 0, new OFMatch20()));

        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
                .withPriority(priority)
                .withCookie(newFlowEntryId)
                .makePermanent();
        flowRuleService.applyFlowRules(flowRule.build());
    }

    public void install_pof_add_vlc_header_entry(DeviceId deviceId, int tableId, String srcIP, int outport, int priority,
                                             byte timeSlot, short ledId, short ueId, short serviceId) {
        // vlc header
        short type = 0x1918;
        short len = 0x000b;      // type:2 + len:2 + ts:1 + ledID:2 + ueID:2 + serviceId:2 = 11
        short vlc_offset = 336;  // begin of udp payload: 42*8=336 bits
        short vlc_length = 88;   // 11 * 8 bits
        short VLC = 0x16;

        // metadata bits
        short metadata_offset = 32;
        short write_len = 16;

        // vlc_header
        StringBuilder vlc_header = new StringBuilder();
        vlc_header.append(short2HexStr(type));
        vlc_header.append(short2HexStr(len));
        vlc_header.append(byte2HexStr(timeSlot));
        vlc_header.append(short2HexStr(ledId));
        vlc_header.append(short2HexStr(ueId));
        vlc_header.append(short2HexStr(serviceId));

        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        ArrayList<Criterion> matchList = new ArrayList<>();
        matchList.add(Criteria.matchOffsetLength(SIP, (short) 208, (short) 32, srcIP, "ffffffff"));
        trafficSelector.add(Criteria.matchOffsetLength(matchList));

        // action: add vlc header
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<>();
        OFAction action_add_vlc_field = DefaultPofActions.addField(VLC, vlc_offset, vlc_length, vlc_header.toString())
                .action();

        // used for set_field_from_metadata
        OFMatch20 metadata_udp_len = new OFMatch20();
        metadata_udp_len.setFieldName("metadata_udp_len");
        metadata_udp_len.setFieldId(OFMatch20.METADATA_FIELD_ID);
        metadata_udp_len.setOffset((short) (vlc_offset + 16));     // the packet_field_offset
        metadata_udp_len.setLength(write_len);                     // the packet_field_len

        // used for modify_field
        OFMatch20 vlc_len_field = new OFMatch20();
        vlc_len_field.setFieldName("vlc_len");
        vlc_len_field.setFieldId(len);
        vlc_len_field.setOffset((short) (vlc_offset + 16));
        vlc_len_field.setLength((short) 16);

        // vlc_len = vlc.header + udp.payload, so metadata minus udp.header
        short vlc_len = (short) (len - 8);
        OFAction action_set_vlc_len = DefaultPofActions.setFieldFromMetadata(metadata_udp_len, metadata_offset)
                .action();
        OFAction action_inc_vlc_len = DefaultPofActions.modifyField(vlc_len_field, vlc_len)
                .action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, outport)
                .action();

        actions.add(action_add_vlc_field);
        actions.add(action_set_vlc_len);
        actions.add(action_inc_vlc_len);
        actions.add(action_output);
        trafficTreatment.add(DefaultPofInstructions.applyActions(actions));

        // get existed flow rules in flow table. if the dstIp equals, then delete it
        Map<Integer, FlowRule> existedFlowRules = new HashMap<>();
        existedFlowRules = flowTableStore.getFlowEntries(deviceId, FlowTableId.valueOf(tableId));
        if(existedFlowRules != null) {
            for(Integer flowEntryId : existedFlowRules.keySet()) {
                log.info("existedFlowRules.get(flowEntryId).selector().equals(trafficSelector.build()) ==> {}",
                        existedFlowRules.get(flowEntryId).selector().equals(trafficSelector.build()));
                if(existedFlowRules.get(flowEntryId).selector().equals(trafficSelector.build())) {
                    flowTableService.removeFlowEntryByEntryId(deviceId, tableId, flowEntryId);
                }
            }
        }

        long newFlowEntryId = flowTableStore.getNewFlowEntryId(deviceId, tableId);
        FlowRule.Builder flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .forTable(tableId)
                .withSelector(trafficSelector.build())
                .withTreatment(trafficTreatment.build())
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
        install_openflow_output_FlowRule(deviceId, tableId = 0);

        /* test grop table */
//        installSelectGroupFlowRule(deviceId, tableId = 0, "abc",0x12);
//        installGroupActionFlowRule(deviceId, tableId = 0, 0x12);   // if just send group_action, then OFBAC_BAD_OUT_GROUP

        /* test second group table send */
//        installSelectGroupFlowRule(deviceId, tableId = 0, "def", 0x24);
//        installGroupActionFlowRule(deviceId, tableId = 0, 0x24);   // if just send group_action, then OFBAC_BAD_OUT_GROUP

        /* test mod_nw_dst */
//        install_openflow_mod_nw_dst_rule(deviceId, tableId = 0);
    }

    public void openflowTestStop() {
        /* test group table */
        removeGroupTables(deviceId, "abc");
        removeGroupTables(deviceId, "def");

        /* remove flow rule by appId */
        removeFlowTable(deviceId, tableId = 0);
        log.info("org.onosproject.openflow.test.action Stopped");
    }

    public void install_openflow_output_FlowRule(DeviceId deviceId, byte tableId) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
         trafficSelector.matchInPort(PortNumber.portNumber(1));
//        trafficSelector.matchEthType(Ethernet.TYPE_IPV4);

        // action: packet in to controller
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        PortNumber out_port = PortNumber.portNumber(2);
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

    public void installGroupActionFlowRule(DeviceId deviceId, byte tableId, int group_id) {
        // match
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();
        trafficSelector.matchEthType(Ethernet.TYPE_IPV4)
                       .matchInPort(PortNumber.portNumber(1));

        // action: packet in to controller
        TrafficTreatment.Builder trafficTreatment = DefaultTrafficTreatment.builder();
        trafficTreatment.group(new GroupId(group_id))
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

    public void installSelectGroupFlowRule(DeviceId deviceId, byte tableId, String key_str, int group_id) {
        GroupId select_group_id1 = new GroupId(group_id);
        byte[] keyData = key_str.getBytes();
        final GroupKey key = new DefaultGroupKey(keyData);

        // bucket1: action = mod_nw_src + output
        TrafficTreatment.Builder trafficTrement_bucket1 = DefaultTrafficTreatment.builder();
        trafficTrement_bucket1.setIpDst(IpAddress.valueOf("10.1.1.2"))
                              .setOutput(PortNumber.portNumber(2))
                              .build();
        short weight1 = 2;
        GroupBucket bucket1 = DefaultGroupBucket.createSelectGroupBucket(trafficTrement_bucket1.build(), weight1);

        // bucket2: action = mod_nw_dst + output
        TrafficTreatment.Builder trafficTrement_bucket2 = DefaultTrafficTreatment.builder();
        trafficTrement_bucket2.setIpDst(IpAddress.valueOf("10.2.2.2"))
                              .setOutput(PortNumber.portNumber(2))
                              .build();
        short weight2 = 3;
        GroupBucket bucket2 = DefaultGroupBucket.createSelectGroupBucket(trafficTrement_bucket2.build(), weight2);

        // buckets
        GroupBuckets select_group_buckets = new GroupBuckets(ImmutableList.of(bucket1, bucket2));

        // apply
        DefaultGroupDescription select_group = new DefaultGroupDescription(deviceId,
                GroupDescription.Type.SELECT, select_group_buckets, key, select_group_id1.id(), appId);
        groupService.addGroup(select_group);

    }

    public void removeGroupTables(DeviceId deviceId, String key_str) {
        byte[] keyData = key_str.getBytes();
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


    /**
     * util tools.
     */

    public String short2HexStr(short shortNum) {
        StringBuilder hex_str = new StringBuilder();
        byte[] b = new byte[2];
        b[1] = (byte) (shortNum & 0xff);
        b[0] = (byte) ((shortNum >> 8) & 0xff);

        return bytes_to_hex_str(b);
    }

    public String byte2HexStr(byte byteNum) {
        String hex = Integer.toHexString(   byteNum & 0xff);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex;
    }

    public String bytes_to_hex_str(byte[] b) {
        StringBuilder hex_str = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xff);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hex_str.append(hex);
        }
        return hex_str.toString();
    }

}
