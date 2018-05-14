package org.flowrule.app;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.action.OFAction;
import org.onosproject.floodlightpof.protocol.table.OFFlowTable;
import org.onosproject.floodlightpof.protocol.table.OFTableType;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.DefaultPofActions;
import org.onosproject.net.flow.instructions.DefaultPofInstructions;
import org.onosproject.net.table.DefaultFlowTable;
import org.onosproject.net.table.FlowTable;
import org.onosproject.net.table.FlowTableId;
import org.onosproject.net.table.FlowTableService;
import org.onosproject.net.table.FlowTableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



/** * Skeletal ONOS application component.
 *
 * */

@Component(immediate = true)

public class AppComponent {

    private static final String RULE_TEST = "org.flowrule.app"; 

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
  *
  * Annotations (e.g. @Reference) within the code -
  * interdependencies between various services, used by Karaf
  * to resolve module dependencies during system startup
  * and when loading bundles at runtime
  */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableStore tableStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowTableService flowTableService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private ApplicationId appId;
    private DeviceId deviceId;
    private byte globalTableId;
    private long newFlowEntryId;

    static FlowRule.Builder flowRule;
    @Activate
    protected void activate() {

        log.info("Started");
        appId = coreService.registerApplication(RULE_TEST);   //register app
        log.info(appId.toString());
        sendPofFlowRule();
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
        getRemovePofFlowRule();
    }


    public void sendPofFlowRule() {
//***********************************flowTable**********************************************
        //deviceId = deviceService.getAvailableDevices().iterator().next().id();
        deviceId = DeviceId.deviceId("pof:0000000000000001");

        globalTableId = (byte) tableStore.getNewGlobalFlowTableId(deviceId, OFTableType.OF_MM_TABLE);

        log.info("globalTableId: {}", globalTableId);

//        byte globalTableId = (byte) 3;

        byte smallTableId = tableStore.parseToSmallTableId(deviceId, globalTableId);
        log.info("smallTableId: {}", smallTableId);

        //construct ofMatch20 object
        OFMatch20 ofMatch20= new OFMatch20();
        ofMatch20.setFieldId((short) 1);
        ofMatch20.setFieldName("FirstEntryTable");
        ofMatch20.setOffset((short) 240); // DIP
        ofMatch20.setLength((short) 32);

        ArrayList<OFMatch20> match20List = new ArrayList<OFMatch20>();
        match20List.add(ofMatch20);

        //construct ofFlowTable
        OFFlowTable ofFlowTable = new OFFlowTable();
        ofFlowTable.setTableId(smallTableId);
        ofFlowTable.setTableName("FirstEntryTable");
        ofFlowTable.setTableSize(32);
        ofFlowTable.setTableType(OFTableType.OF_MM_TABLE);
        ofFlowTable.setMatchFieldList(match20List);

        //build FlowTable

        log.info("+++++ before build flowtable:" + appId);
        FlowTable.Builder flowTable = DefaultFlowTable.builder()
                .withFlowTable(ofFlowTable)
                .forTable(globalTableId)
                .forDevice(deviceId)
                .fromApp(appId);

        log.info("+++++ before applyFlowTables");
        flowTableService.applyFlowTables(flowTable.build());   //ofFlowTable now ok
//        handleConnectionUp();

//*********************flowRule************************************************************

        //get a new flow entry Id
        newFlowEntryId = tableStore.getNewFlowEntryId(deviceId, globalTableId);

        log.info("++++ newFlowEntryId: {}", newFlowEntryId);
        //match
       // TrafficSelector.Builder pbuilder = DefaultTrafficSelector.builder();

        String hoa = "0a11a411";
        String coa = "c0a8037f";
        //**** match field ****
        TrafficSelector.Builder pbuilder = DefaultTrafficSelector.builder();
        ArrayList<Criterion> list = new ArrayList<Criterion>();
        list.add(Criteria.matchOffsetLength((short) 1, (short) 240, (short) 32, hoa, "ffFFffFF" ));
//        list.add(Criteria.matchOffsetLength((short) 1,(short)0,(short)48,"000000000002","ffffffffffff"));
//        list.add(Criteria.matchOffsetLength((short) 2,(short)96,(short)16,"0800","ffff"));
        pbuilder.add(Criteria.matchOffsetLength(list));

        //instructions/actions
        TrafficTreatment.Builder ppbuilder = DefaultTrafficTreatment.builder();
        List<OFAction> actions = new ArrayList<OFAction>();

        //**** actions ****
        int port1 = 1;
        int port2 = 2;
        int port3 = 3;
        int port4 = 4;
        OFAction action_setField = DefaultPofActions.setField((short) 1, (short) 240, (short) 32, coa, "ffFFffFF").action();
        OFAction action_output = DefaultPofActions.output((short) 0, (short) 0, (short) 0, port1).action();
        actions.add(action_setField);
        actions.add(action_output);
        /*actions.add(DefaultPofActions.addField((short) 0,(short) 0, 48,"010203040506").action());
        actions.add(DefaultPofActions.setField((short) 0,(short) 0, 48,"112233445566","ffffffffffff").action());
        actions.add(DefaultPofActions.output((short)0, (short)0, (short)0, 0).action());*/

        //**** ins ****
        ppbuilder.add(DefaultPofInstructions.applyActions(actions));

        TrafficSelector selector = pbuilder.build();
        TrafficTreatment treatment = ppbuilder.build();



        flowRule = DefaultFlowRule.builder()
                .forTable(globalTableId)
                .forDevice(deviceId)
                .withSelector(selector)
                .withTreatment(treatment)
                .withPriority(1)
                .makePermanent()
                .withCookie(newFlowEntryId);//to set flow entryId

        log.info("++++++++deviceId: {}", flowRule.toString());
        //flowRuleService.applyRule(flowRule);
        flowRuleService.applyFlowRules(flowRule.build());
    }

    public void getRemovePofFlowRule() {
        deviceId = deviceService.getAvailableDevices().iterator().next().id();

        //flowTableService.removeFlowEntryByEntryId(deviceId, globalTableId, newFlowEntryId); 
        log.info("++++ before removeFlowTablesByTableId: {}", globalTableId);

        flowTableService.removeFlowTablesByTableId(deviceId, FlowTableId.valueOf(globalTableId));
        //flowRuleService.removeFlowRulesById(appId);
    }


}