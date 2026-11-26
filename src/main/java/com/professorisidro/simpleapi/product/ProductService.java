package com.professorisidro.simpleapi.product;

import com.professorisidro.simpleapi.common.BusinessException;
import com.professorisidro.simpleapi.common.ResourceNotFoundException;
import com.professorisidro.simpleapi.order.OrderItemRepository;
import com.professorisidro.simpleapi.product.dto.ProductRequest;
import com.professorisidro.simpleapi.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException("A product with SKU '" + request.sku() + "' already exists");
        }
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new BusinessException("A product with SKU '" + request.sku() + "' already exists");
        }
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = getEntity(id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new BusinessException("Cannot delete product referenced by existing orders");
        }
        productRepository.delete(product);
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSku(request.sku());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
    }

    static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity());
    }
}
