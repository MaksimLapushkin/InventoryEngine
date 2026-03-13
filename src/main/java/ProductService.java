import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository){
        this.repository=repository;
    }

    public void addProduct(Product product){
        if (repository.findById(product.getId()).isPresent()){
            throw new IllegalStateException("product already exists");
        }
        repository.save(product);
    }
    public Optional<Product> getProduct(int id){
        Optional<Product> pr =  repository.findById(id);
        if (pr.isEmpty()){
            throw new IllegalArgumentException("No such product found");
        }
        return pr;
    }
    public List<Product> listProducts(){
        return repository.findAll();
    }
    public List<Product> findProductsByUnit(Unit unit){
        return  repository.findAll().stream()
                .filter(product -> product.getUnit()==unit)
                .collect(Collectors.toList());
    }
    public List<Product> findProductsByName(String name){
        if (name==null || name.isEmpty()){
            throw new IllegalArgumentException("empty name");
        }
        return repository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

    }

}
