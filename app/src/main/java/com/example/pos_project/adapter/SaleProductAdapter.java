package com.example.pos_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pos_project.R;
import com.example.pos_project.api.ApiClient;
import com.example.pos_project.model.Product;

import java.util.List;

public class SaleProductAdapter extends RecyclerView.Adapter<SaleProductAdapter.SaleProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onAddToCartClick(Product product);
    }

    public SaleProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SaleProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale_product, parent, false);
        return new SaleProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleProductViewHolder holder, int position) {
        android.util.Log.d("SaleProductAdapter", "Binding view at position " + position);
        if (productList != null && position < productList.size()) {
            Product product = productList.get(position);
            android.util.Log.d("SaleProductAdapter", "Product at position " + position + ": " + product.getName());
            holder.bind(product);
        } else {
            android.util.Log.e("SaleProductAdapter", "Invalid position or null list. Position: " + position + ", List size: " + (productList != null ? productList.size() : 0));
        }
    }

    @Override
    public int getItemCount() {
        int count = productList != null ? productList.size() : 0;
        android.util.Log.d("SaleProductAdapter", "getItemCount called. Size: " + count);
        return count;
    }

    class SaleProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvProductPrice, tvStockCountOverlay;
        private android.widget.ImageButton btnAddToCart;
        private android.widget.ImageView ivProductImage;

        public SaleProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvStockCountOverlay = itemView.findViewById(R.id.tv_stock_count_overlay);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format("$%.2f", product.getPrice()));
            
            // Set stock count overlay - ensure it's never negative
            int stockCount = Math.max(0, product.getQuantity());
            tvStockCountOverlay.setText(String.valueOf(stockCount));

            // Load product image with Glide
            android.util.Log.d("SaleProductAdapter", "Product: " + product.getName() + ", Image URL: " + product.getImage());
            
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                String originalUrl = product.getImage();
                String imageUrl = ApiClient.fixImageUrl(originalUrl);
                
                android.util.Log.d("SaleProductAdapter", "Original URL: " + originalUrl);
                android.util.Log.d("SaleProductAdapter", "Fixed URL: " + imageUrl);
                
                Glide.with(ivProductImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, 
                                                  com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                  boolean isFirstResource) {
                            android.util.Log.e("SaleProductAdapter", "Image load failed for: " + imageUrl);
                            if (e != null) {
                                android.util.Log.e("SaleProductAdapter", "Glide error details: " + e.getMessage());
                                for (Throwable cause : e.getRootCauses()) {
                                    android.util.Log.e("SaleProductAdapter", "Root cause: " + cause.getMessage());
                                }
                            }
                            return false; // Let Glide handle the error
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                                     com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                     com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            android.util.Log.d("SaleProductAdapter", "Image loaded successfully for: " + imageUrl);
                            return false; // Let Glide handle the success
                        }
                    })
                    .into(ivProductImage);
            } else {
                android.util.Log.d("SaleProductAdapter", "No image URL provided, showing placeholder");
                // Show placeholder if no image
                ivProductImage.setImageResource(R.drawable.placeholder_image);
            }

            // Style the add to cart button based on stock (keep enabled for click handling)
            btnAddToCart.setEnabled(true);  // Always keep enabled to handle clicks
            if (product.getQuantity() == 0) {
                btnAddToCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getColor(android.R.color.darker_gray)));
                btnAddToCart.setAlpha(0.5f);
            } else {
                btnAddToCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getColor(com.example.pos_project.R.color.primary)));
                btnAddToCart.setAlpha(1.0f);
            }

            btnAddToCart.setOnClickListener(v -> {
                android.util.Log.d("SaleProductAdapter", "Add to cart clicked for product: " + product.getName() + ", quantity: " + product.getQuantity());
                if (listener != null) {
                    if (product.getQuantity() > 0) {
                        android.util.Log.d("SaleProductAdapter", "Adding to cart: " + product.getName());
                        listener.onAddToCartClick(product);
                    } else {
                        android.util.Log.d("SaleProductAdapter", "Showing out of stock dialog for: " + product.getName());
                        showOutOfStockDialog(v.getContext(), product.getName());
                    }
                }
            });


        }
    }

    private void showOutOfStockDialog(android.content.Context context, String productName) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomDialogTheme);
        androidx.appcompat.app.AlertDialog dialog = builder
            .setTitle("Out of Stock")
            .setMessage("Sorry, \"" + productName + "\" is currently out of stock.")
            .setPositiveButton("OK", null)
            .create();
        
        dialog.show();
        
        // Set dialog width to 80% of screen width
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
            android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                layoutParams.width = (int) (displayMetrics.widthPixels * 0.8);
                window.setAttributes(layoutParams);
            }
        }
    }
}