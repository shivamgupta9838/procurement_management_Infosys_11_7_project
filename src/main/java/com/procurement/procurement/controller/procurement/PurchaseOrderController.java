package com.procurement.procurement.controller.procurement;

import com.procurement.procurement.entity.procurement.PurchaseOrder;
import com.procurement.procurement.service.procurement.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/procurement/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    // ===================== Create Purchase Order =====================
    @PostMapping("/create")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder po = purchaseOrderService.createPurchaseOrder(purchaseOrder);
        return ResponseEntity.ok(po);
    }

    // ===================== Get all Purchase Orders =====================
    @GetMapping("/all")
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(orders);
    }

    // ===================== Get Purchase Order by ID =====================
    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseOrderById(@PathVariable Long id) {
        Optional<PurchaseOrder> poOpt = purchaseOrderService.getPurchaseOrderById(id);
        if (poOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Purchase Order not found");
        }
        return ResponseEntity.ok(poOpt.get());
    }

    // ===================== Update Purchase Order Status =====================
    @PatchMapping("/update-status/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable Long id,
            @RequestParam String status) {
        try {
            purchaseOrderService.updatePurchaseOrderStatus(id, status);
            return ResponseEntity.ok("Purchase Order status updated to " + status);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
