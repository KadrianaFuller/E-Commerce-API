package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {


    private List<Category> categories;

    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }


    // get all categories
    @Override
    public List<Category> getAllCategories() {
        this.categories.clear();
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loops through result set and add each category to the list
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.categories;
    }

    // get category by id
    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        Category category = null;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);// setting Category Id parameter
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    category = mapRow(rs);  // Use mapRow to map the result to a Category object
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return category;

    }

    // create a new category
    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return category;

    }

    // update category
    @Override
    public void update(int categoryId, Category category) {
        int namePos = 0;
        int descriptionPos = 0;
        int idPos = 0;
        String updateParamStatement = "";

        // Check if name is not null and add it to the update statement
        if (category.getName() != null) {
            namePos += 1;
            idPos++;
            updateParamStatement += "name = ? ";
        }

        // Check if description is not null and add it to the update statement
        if (category.getDescription() != null) {
            descriptionPos += namePos + 1;
            idPos++;
            String comma = "";
            if (updateParamStatement.length() > 0) {
                comma = ",";
            }
            updateParamStatement += comma + "description = ? ";
        }

        // Construct the SQL query with placeholders
        String sql = "UPDATE categories SET " + updateParamStatement + " WHERE category_id = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            // Set parameters in the statement
            if (category.getName() != null) {
                stmt.setString(namePos, category.getName());
            }

            if (category.getDescription() != null) {
                stmt.setString(descriptionPos, category.getDescription());
            }

            // Set the categoryId at the end of the statement
            stmt.setInt(idPos + 1, categoryId);

            // Execute the update
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete category
    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Set the categoryId parameter in the SQL query
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // helper method used to map the RS to a Category object
    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
