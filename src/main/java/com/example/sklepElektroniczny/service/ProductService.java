package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.dtos.ProductResponse;
import com.example.sklepElektroniczny.entity.Category;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.CategoryRepository;
import com.example.sklepElektroniczny.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements ProductServiceInterface{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductPresent = false;

        List<Product> products = category.getProducts();
        for (Product product : products) {
            if (product.getProductName().equals(productDTO.getProductName())) {
                isProductPresent = true;
                break;
            }
        }

        if(!isProductPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new APIException("Product already exists");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page = productRepository.findAll(pageable);

        List<Product> products = page.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

        List<Product> products = page.getContent();

        if(products.isEmpty()){
            throw new APIException("Product not found with this category");
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> page = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageable);

        List<Product> products = page.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("Product not found with this keyword");
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product1 = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        product1.setProductName(product.getProductName());
        product1.setDescription(product.getDescription());
        product1.setQuantity(product.getQuantity());
        product1.setDiscount(product.getDiscount());
        product1.setPrice(product.getPrice());
        product1.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(product1);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile imageFile) throws IOException {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String uploadDirectory = "images/";
        String savedFileName = saveImageToFileSystem(uploadDirectory, imageFile);

        existingProduct.setImage(savedFileName);

        Product updatedProduct = productRepository.save(existingProduct);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private String saveImageToFileSystem(String directoryPath, MultipartFile imageFile) throws IOException {

        String originalFileName = imageFile.getOriginalFilename();

        String uniqueId = UUID.randomUUID().toString();
        String newFileName = uniqueId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String fullPath = directoryPath + File.separator + newFileName;

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        Files.copy(imageFile.getInputStream(), Paths.get(fullPath));

        return newFileName;
    }
}
