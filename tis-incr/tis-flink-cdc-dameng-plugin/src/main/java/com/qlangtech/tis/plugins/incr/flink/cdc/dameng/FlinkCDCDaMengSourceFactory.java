package com.qlangtech.tis.plugins.incr.flink.cdc.dameng;

import com.google.common.collect.Lists;
import com.qlangtech.plugins.incr.flink.cdc.FlinkCol;
import com.qlangtech.tis.async.message.client.consumer.IFlinkColCreator;
import com.qlangtech.tis.async.message.client.consumer.IMQListener;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceMeta;
import io.debezium.config.Field;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.flink.cdc.connectors.base.options.StartupOptions;
import org.devlive.connector.dameng.DamengConnectorConfig;

import java.util.List;
import java.util.function.Function;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/27
 */
public class FlinkCDCDaMengSourceFactory extends MQListenerFactory {

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String startupOptions;

    @FormField(ordinal = 2, type = FormFieldType.ENUM, validate = {Validator.require})
    public String miningStrategy;

    //    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {Validator.require})
    //    public Boolean lob;

    @FormField(ordinal = 4, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer poolInterval;

    /**
     * https://debezium.io/documentation/reference/1.9/connectors/oracle
     * .html#oracle-property-event-processing-failure-handling-mode
     */
    @FormField(ordinal = 5, type = FormFieldType.ENUM, validate = {Validator.require})
    public String failureHandle;

    /**
     * binlog监听在独立的slot中执行
     */
    @FormField(ordinal = 99, advance = true, type = FormFieldType.ENUM, validate = {Validator.require})
    public boolean independentBinLogMonitor;

    //    private static Field createLobField() {
    //        Field old = DamengConnectorConfig.LOB_ENABLED;
    //        //  String name, String displayName, String description, String defaultValue
    //        return Field.create(old.name(), "Supports mining LOB fields and operations", old.description(), new
    //        BooleanSupplier() {
    //            @Override
    //            public boolean getAsBoolean() {
    //                return (Boolean) old.defaultValue();
    //            }
    //        });
    //    }
    StartupOptions getStartupOptions() {
        switch (startupOptions) {
            case "latest":
                return StartupOptions.latest();
            case "initial":
                return StartupOptions.initial();
            default:
                throw new IllegalStateException("illegal startupOptions:" + startupOptions);
        }
    }
    @Override
    public IFlinkColCreator<FlinkCol> createFlinkColCreator(DataSourceMeta sourceMeta) {
        return (meta, colIndex) -> {
            return meta.getType().accept(new DaMengCDCTypeVisitor(meta, colIndex));
        };
    }

    public static List<Triple<String, Field, Function<FlinkCDCDaMengSourceFactory, Object>>> debeziumProps =
            Lists.newArrayList(
            //Triple.of("lob", createLobField(), (sf) -> sf.lob)
            Triple.of("poolInterval", DamengConnectorConfig.POLL_INTERVAL_MS, (sf) -> sf.poolInterval), Triple.of(
                    "failureHandle", DamengConnectorConfig.EVENT_PROCESSING_FAILURE_HANDLING_MODE,
                            (sf) -> sf.failureHandle), Triple.of("miningStrategy",
                            DamengConnectorConfig.LOG_MINING_STRATEGY, (sf) -> sf.miningStrategy));

    @Override
    public IMQListener create() {
        return new FlinkCDCDaMengSourceFunction(this);
    }
}
