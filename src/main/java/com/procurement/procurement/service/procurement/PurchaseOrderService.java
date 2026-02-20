package com.procurement.procurement.service.procurement;

import com.procurement.procurement.entity.procurement.PurchaseOrder;
import com.procurement.procurement.entity.vendor.Vendor;
import com.procurement.procurement.repository.procurement.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private com.procurement.procurement.repository.vendor.VendorRepository vendorRepository;

    @Autowired
    private com.procurement.procurement.repository.procurement.PurchaseOrderItemRepository purchaseOrderItemRepository;

    // ===================== Create new Purchase Order =====================
    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
        // Fetch full vendor details
        if (purchaseOrder.getVendor() != null && purchaseOrder.getVendor().getId() != null) {
            Vendor vendor = vendorRepository.findById(purchaseOrder.getVendor().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Vendor not found with id: " + purchaseOrder.getVendor().getId()));
            purchaseOrder.setVendor(vendor);
        }

        // Set timestamps
        purchaseOrder.setCreatedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedAt(LocalDateTime.now());

        // Save purchase order first to get the ID
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);

        // Save items if present
        if (purchaseOrder.getItems() != null) {
            for (com.procurement.procurement.entity.procurement.PurchaseOrderItem item : purchaseOrder.getItems()) {
                item.setPurchaseOrder(savedPO);
                purchaseOrderItemRepository.save(item);
            }
        }

        return savedPO;
    }

    // ===================== Update Purchase Order =====================
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrder updatedPO) {

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + id));

        po.setVendor(updatedPO.getVendor());
        po.setItems(updatedPO.getItems());
        po.setStatus(updatedPO.getStatus());
        po.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(po);
    }

    // ===================== Get all Purchase Orders =====================
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    // ===================== Get Purchase Orders by Vendor =====================
    public List<PurchaseOrder> getPurchaseOrdersByVendor(Vendor vendor) {
        return purchaseOrderRepository.findByVendor(vendor);
    }

    // ===================== Get Purchase Orders by Status =====================
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    // ===================== Get Purchase Order by ID =====================
    public Optional<PurchaseOrder> getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    // ===================== Update Purchase Order Status =====================
    public PurchaseOrder updatePurchaseOrderStatus(Long id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + id));
        po.setStatus(status);
        po.setUpdatedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(po);
    }
}
