package com.vCampus.net;

import com.vCampus.common.ConfigManager;
import com.vCampus.net.dto.ShopDtos;
import com.vCampus.net.dto.SocketRequest;
import com.vCampus.net.dto.SocketResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSocketClient {
    private final String host;
    private final int port;

    public ShopSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static ShopSocketClient fromConfig() {
        return new ShopSocketClient(ConfigManager.getSocketServerHost(), ConfigManager.getSocketServerPort());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> listProducts(String category, String keyword) {
        Map<String,String> params = new HashMap<>();
        if (category != null) params.put("category", category);
        if (keyword != null) params.put("keyword", keyword);
        SocketResponse resp = send(new SocketRequest("SHOP_LIST", params));
        if (resp == null || !resp.isSuccess()) return List.of();
        Object data = resp.getData();
        if (data instanceof Map<?,?> m && m.get("rows") instanceof List<?> rows) {
            List<Map<String,Object>> out = new ArrayList<>();
            for (Object r : rows) if (r instanceof Map<?,?> rm) out.add((Map<String,Object>)rm);
            return out;
        }
        return List.of();
    }

    public ShopDtos.CreateOrderRespDTO createOrder(String studentId, List<ShopDtos.OrderItemDTO> items) {
        ShopDtos.CreateOrderReqDTO dto = new ShopDtos.CreateOrderReqDTO();
        dto.studentId = studentId;
        dto.items.addAll(items);
        SocketRequest req = new SocketRequest("SHOP_CREATE_ORDER", Map.of(), dto);
        SocketResponse resp = send(req);
        if (resp == null || !resp.isSuccess()) return null;
        Object data = resp.getData();
        if (data instanceof Map<?,?> m && m.get("order") instanceof ShopDtos.CreateOrderRespDTO d) return d;
        return null;
    }

    public boolean payOrder(String orderId) {
        SocketRequest req = new SocketRequest("SHOP_PAY", Map.of("orderId", orderId));
        SocketResponse resp = send(req);
        return resp != null && resp.isSuccess();
    }

    private SocketResponse send(SocketRequest req) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(ConfigManager.getSocketSoTimeoutMs());
            try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
                out.writeObject(req);
                out.flush();
                Object obj = in.readObject();
                if (obj instanceof SocketResponse resp) return resp;
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}


