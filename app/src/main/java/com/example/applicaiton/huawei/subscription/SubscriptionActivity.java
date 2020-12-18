/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.example.applicaiton.huawei.subscription;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.applicaiton.huawei.R;
import com.example.applicaiton.huawei.common.Constants;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;


import java.util.Arrays;
import java.util.List;

/**
 * Activity for auto-renewable subscriptions.
 *
 * @since 2019/12/9
 */
public class SubscriptionActivity extends Activity implements SubscriptionContract.View {

    private static final String TAG = "SubscriptionActivity";
    private static final String[] SUBSCRIPTION_PRODUCT = new String[]{"subscriptionexample001"};
    private SubscriptionContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.INVISIBLE);

        List<String> list = Arrays.asList(SUBSCRIPTION_PRODUCT);
        presenter = new SubscriptionPresenter(this);
        presenter.load(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (resultCode == Activity.RESULT_OK) {
                int purchaseResult = SubscriptionUtils.getPurchaseResult(this, data);
                if (OrderStatusCode.ORDER_STATE_SUCCESS == purchaseResult) {
                    Toast.makeText(this, R.string.pay_success, Toast.LENGTH_SHORT).show();
                    presenter.refreshSubscription();
                    return;
                }
                if (OrderStatusCode.ORDER_STATE_CANCEL == purchaseResult) {
                    Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.pay_fail, Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "cancel subscribe");
                Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void showProducts(List<ProductInfo> productInfos) {
        if (null == productInfos) {
            Toast.makeText(this, R.string.external_error, Toast.LENGTH_SHORT).show();
            return;
        }

        for (ProductInfo productInfo : productInfos) {
            showProduct(productInfo);
        }

        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);
    }

    @Override
    public void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult) {
        for (String productId : SUBSCRIPTION_PRODUCT) {
            View view = getView(productId);
            Button button = view.findViewById(R.id.action);
            button.setTag(productId);
            if (SubscriptionUtils.shouldOfferService(ownedPurchasesResult, productId)) {
                button.setText(R.string.active);
                button.setOnClickListener(getDetailActionListener());
            } else {
                button.setText(R.string.buy);
                button.setOnClickListener(getBuyActionListener());
            }
        }
    }

    /**
     * Jump to manage subscription page
     * @param view the view which has been clicked
     */
    public void manageSubscription(View view) {
        presenter.showSubscription("");
    }

    private void showProduct(ProductInfo productInfo) {
        View view = getView(productInfo.getProductId());

        if (view != null) {
            TextView productName = view.findViewById(R.id.product_name);
            TextView productDesc = view.findViewById(R.id.product_desc);
            TextView price = view.findViewById(R.id.price);

            productName.setText(productInfo.getProductName());
            productDesc.setText(productInfo.getProductDesc());
            price.setText(productInfo.getPrice());

        }
    }

    private View getView(String productId) {
        View view = null;
        if (SUBSCRIPTION_PRODUCT[0].equals(productId)) {
            view = findViewById(R.id.service_one_product_one);
        }
        return view;
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    private View.OnClickListener getBuyActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object data = v.getTag();
                if (data instanceof String) {
                    String productId = (String) data;
                    presenter.buy(productId);
                }
            }
        };
    }

    private View.OnClickListener getDetailActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object data = v.getTag();
                if (data instanceof String) {
                    String productId = (String) data;
                    presenter.showSubscription(productId);
                }
            }
        };
    }
}
