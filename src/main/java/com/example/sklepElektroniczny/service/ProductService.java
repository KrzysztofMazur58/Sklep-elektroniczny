package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.dtos.ProductResponse;
import com.example.sklepElektroniczny.entity.Category;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.rabbitmq.MessageProducer;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.repository.CategoryRepository;
import com.example.sklepElektroniczny.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService implements ProductServiceInterface{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final MessageProducer messageProducer;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CartRepository cartRepository,
                          CartService cartService,
                          ModelMapper modelMapper,
                          MessageProducer messageProducer) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
        this.messageProducer = messageProducer;
    }

    public void setImageBaseUrl(String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
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

            String message = "Dodano produkt: " + savedProduct.getProductName();
            messageProducer.sendProductCreatedMessage(message);

            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new APIException("Product already exists");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Specification<Product> specification = Specification.where(null);

        if(keyword != null && !keyword.isEmpty()){
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
        }

        if(category != null && !category.isEmpty()){
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("category").get("categoryName"), category));
        }

        Page<Product> page = productRepository.findAll(specification, pageable);

        List<Product> products = page.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(createUrlForImage(product.getImage()));
                    return productDTO;
                })
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
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return modelMapper.map(product, ProductDTO.class);
    }

    private String createUrlForImage(String name){
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + name : imageBaseUrl + "/" + name;
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

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartElements().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        if (!carts.isEmpty()) {

            throw new APIException("Nie można usunąć produktu, ponieważ jest on obecny w jednym lub więcej koszykach.");
        }

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
