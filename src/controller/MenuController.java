package controller;

import dao.MenuDAO;
import model.MenuItem;

import java.sql.SQLException;
import java.util.List;

/** MVC Controller — Admin menu management (FR-5) */
public class MenuController {

    private final MenuDAO menuDAO;

    public MenuController() {
        this.menuDAO = new MenuDAO();
    }

    public List<MenuItem> getAllItems() throws SQLException {
        return menuDAO.getAllItems();
    }

    public List<MenuItem> getAvailableItems() throws SQLException {
        return menuDAO.getAvailableItems();
    }

    public void addItem(MenuItem item) throws SQLException {
        if (item.getName() == null || item.getName().isBlank())
            throw new IllegalArgumentException("Item name cannot be empty.");
        if (item.getBasePrice() <= 0)
            throw new IllegalArgumentException("Price must be greater than zero.");
        menuDAO.insert(item);
    }

    public void updateItem(MenuItem item) throws SQLException {
        menuDAO.update(item);
    }

    public void deleteItem(int id) throws SQLException {
        menuDAO.delete(id);
    }
}
