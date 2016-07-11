package com.digitalpetri.modbus.examples.slave;

import java.util.concurrent.ExecutionException;

import com.digitalpetri.modbus.requests.ReadCoilsRequest;
import com.digitalpetri.modbus.responses.ReadCoilsResponse;
import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlaveExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new SlaveExample().start();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ModbusTcpSlaveConfig config = new ModbusTcpSlaveConfig.Builder().build();
    private final ModbusTcpSlave slave = new ModbusTcpSlave(config);

    public SlaveExample() {
    }

    public void start() throws ExecutionException, InterruptedException {
        slave.setRequestHandler(new ServiceRequestHandler() {
            @Override
            public void onReadCoils(ServiceRequest<ReadCoilsRequest, ReadCoilsResponse> service) {
                ReadCoilsRequest request = service.getRequest();
                //System.out.println("Request is" + request);

                ByteBuf registers = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());

                for (int i = 0; i < request.getQuantity(); i++) {
                    registers.writeShort(i);
                    registers.writeShort(5);
                    //System.out.println(registers.getCoilStatus(i));
                }
                
                //ReadCoilsResponse response = new ReadCoilsResponse(registers);
                service.sendResponse(new ReadCoilsResponse(registers));
                //System.out.println(new ReadCoilsResponse(registers));
                

                ReferenceCountUtil.release(request);
            }
        });

        slave.bind("localhost", 30000).get();
    }

    public void stop() {
        slave.shutdown();
    }
}
