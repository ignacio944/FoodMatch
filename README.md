# FoodMatch

FoodMatch is a recipe management application that suggests dishes based on the ingredients and quantities available. It also provides nutritional information for each ingredient and recipe, allows dietary restrictions (e.g., vegetarian or gluten-free), and enables users to create and store their own recipes.  

This project was developed as part of the IB Computer Science Internal Assessment and includes full documentation in PDF as well as a demonstration video.

---

## Main Features
- Search recipes according to available ingredients and quantities.
- Create, edit, and save personal recipes.
- Detailed nutritional information per ingredient and recipe.
- Dietary restriction filters (vegetarian, gluten-free, etc.).
- History of cooked and saved recipes.
- User login and profile management.
- MySQL database connection.

---

## Technologies Used
- Language: Java
- Database: MySQL
- Library: mysql-connector-j
- Development Environment: Visual Studio Code (or equivalent)

---

## Project Structure
/Producto → Java source code (classes, UI, database layer)
/Documentación → PDFs with IB documentation
Portada.htm → Index page with links to documentation and product
LICENSE → MIT License


---

## Installation and Execution

### Requirements
- Java 17 (or compatible version)
- MySQL 8.x
- JDBC connector (`mysql-connector-j` included in `/Producto`)

### Steps
1. Clone the repository:
git clone https://github.com/ignacio944/FoodMatch.git
2. Configure the database in MySQL (create database `foodmatch` or the one defined in the code).
3. Review the file `Producto/BasesDeDatos.java` and adjust credentials if needed.
4. Open the project in your IDE.
5. Run the main class (`InicioDeSesion.java` in `/Producto`).

---

## Documentation
The following documents are included in the repository and can be accessed from the [Portada](Portada.htm):

- Criterion A: Planning  
- Criterion B: Design and Task Record  
- Criterion C: Development  
- Criterion D: Product Video (AVI)  
- Criterion E: Evaluation  
- Appendix  
- User Comments  

---

## Author
**Ignacio Bel Iskhakava**  
Developer and author of the FoodMatch project

---

## License
This project is released under the MIT License. See the `LICENSE` file for details.

