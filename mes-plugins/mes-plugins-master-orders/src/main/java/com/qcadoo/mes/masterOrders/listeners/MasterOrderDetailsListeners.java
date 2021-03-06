/**
 * ***************************************************************************
 * Copyright (c) 2018 RiceFish Limited
 * Project: SmartMES
 * Version: 1.6
 *
 * This file is part of SmartMES.
 *
 * SmartMES is Authorized software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.hooks.MasterOrderDetailsHooks;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MasterOrderDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    public void clearAddress(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent address = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);
        address.setFieldValue(null);
    }

    public void onProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName("orders");
        RibbonActionItem createOrder = (RibbonActionItem) orders.getItemByName("createOrder");
        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrder.setEnabled(false);
        } else if (masterOrderProductsGrid.getSelectedEntities().size() == 1) {
            createOrder.setEnabled(true);
        } else {
            createOrder.setEnabled(false);
        }
        createOrder.requestUpdate(true);
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        masterOrderForm.performEvent(view, "refresh");
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = masterOrderForm.getEntity();

        Long masterOrderId = masterOrder.getId();

        if (masterOrderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.masterOrder", masterOrderId);

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);
        Entity entity = masterOrderProductsGrid.getSelectedEntities().get(0);
        Entity product = entity.getBelongsToField(MasterOrderProductFields.PRODUCT);
        parameters.put("form.masterOrderProduct", product.getId());
        parameters.put("form.masterOrderProductComponent", entity.getId());


        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

}
