package org.store.sage;

import org.store.core.beans.Job;
import org.store.core.beans.Order;
import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.sage.bean.SAGEOrder;
import org.hibernate.Hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SageOrdersTask extends SageTask {

    private Integer mainDeposit;
    private Long onlyOrder;
    private String shippingLineName = "PORTES";

    public SageOrdersTask(String storeCode, Job job, Store20Database databaseConfig) throws Exception {
        super(storeCode, job, databaseConfig);
    }

    @Override
    protected void execute() {
        setExecutionPercent(0d);

        // Exportar ordenes
        setExecutionMessage("Buscando ordenes para exportar...");
        shippingLineName = getProperty(SageOrdersJob.PROP_SHIPPING_LINE_NAME,SageOrdersJob.PROP_SHIPPING_LINE_NAME_DEFAULT);
        List<Order> ordersToExport = getOrdersToExport(onlyOrder);
        int numOrders = 0, index = 0, totalOrders = ordersToExport.size();
        for (Order order : ordersToExport) {
            setExecutionPercent(100d * index / totalOrders);
            setExecutionMessage("Exportando orden " + String.valueOf(index++) + "/" + String.valueOf(totalOrders));
            if (!orderInSage(order)) {
                if (exportOrder(order)) {
                    order.setExportedDate(Calendar.getInstance().getTime());
                    dao.save(order);
                    numOrders++;
                }
                dao.flushSession();
                dao.clearSession();
            } else {
                order.setExportedDate(Calendar.getInstance().getTime());
                dao.save(order);
            }
        }
        addOutputMessage(numOrders + " ordenes exportadas");
    }

    private boolean exportOrder(Order order) {
        SAGEOrder sageOrder = new SAGEOrder();
        sageOrder.setShippingLineName(shippingLineName);
        if (sageOrder.importOrder(dao, sageConnection, order, defaultLanguage, getMainDeposit())) {
            // asignar transportacion
            Integer sageCarrier = null;
            if (order.getShippingMethod()!=null) {
                sageCarrier = SomeUtils.strToInteger(properties.getProperty(SageOrdersJob.PROP_ORDER_CARRIER_PREFIX+order.getShippingMethod().getId().toString()));
            } else if (order.getPickInStore()!=null) {
                sageCarrier = SomeUtils.strToInteger(properties.getProperty(SageOrdersJob.PROP_ORDER_PICK_IN_STORE));
            }
            if (sageCarrier!=null) sageOrder.setDO_Expedit(sageCarrier);

            // salvar orden
            if (sageOrder.insert(sageConnection)) return true;
            else addOutputMessage(sageOrder.getLastError());
        } else {
            addOutputMessage(sageOrder.getLastError());
        }
        return false;
    }

    private boolean orderInSage(Order order) {
        try {
            PreparedStatement stmt = sageConnection.prepareStatement("select count(1) from F_DOCENTETE where DO_Piece=?");
            stmt.setString(1, "WEB" + order.getIdOrder().toString());
            ResultSet rs = stmt.executeQuery();
            return (rs.next() && rs.getInt(1) > 0);
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
            addOutputMessage("Error verificando orden " + order.getIdOrder() + "(" + e.getMessage() + ")");
        }
        return true;
    }

    private List<Order> getOrdersToExport(Long idOrder) {
        Order order = (Order) dao.get(Order.class, idOrder);
        if (order!=null) {
            List<Order> result = new ArrayList<Order>();
            result.add(order);
            return result;
        } else {
            String[] statuses = getProperty(SageOrdersJob.PROP_ORDER_STATUS_TO_EXPORT,SageOrdersJob.PROP_ORDER_STATUS_TO_EXPORT_DEFAULT).split(",");
            List<Order> list = dao.getOrdersToExport(statuses);
            for(Order o : list) {
                for(OrderDetail d : o.getOrderDetails()) {
                    Hibernate.initialize(d);
                    for(OrderDetailProduct dp : d.getOrderDetailProducts()) {
                        Hibernate.initialize(dp);
                        Hibernate.initialize(dp.getProduct());
                        Hibernate.initialize(dp.getProduct().getProductLangs());
                    }
                }
            }
            return list;
        }
    }


    public Integer getMainDeposit() {
        if (mainDeposit == null) {
            try {
                PreparedStatement stmt = sageConnection.prepareStatement("select DE_No from F_DEPOT where DE_Principal=1");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) mainDeposit = rs.getInt(1);
                else mainDeposit = 1;
            } catch (SQLException e) {
                log.error(e.getMessage(), e); 
            }
        }
        return mainDeposit;
    }

    public void setOnlyOrder(Long onlyOrder) {
        this.onlyOrder = onlyOrder;
    }
}
