import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentManagementSystemMySQL extends JFrame {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/student_management_system";

    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private Connection connection;
    private JTextArea resultArea;

    public StudentManagementSystemMySQL() {
        super("Student Management System");

        initDatabase();
        initComponents();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initDatabase() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

            // Create the "students" table if not exists
            String createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "age INT NOT NULL)";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createTableSQL);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
            System.exit(1);
        }
    }

    private void initComponents() {
        JPanel buttonPanel = new JPanel();
        JButton viewAllButton = new JButton("View All Students");
        JButton addButton = new JButton("Add Student");

        viewAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAllStudents();
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        buttonPanel.add(viewAllButton);
        buttonPanel.add(addButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    private void displayAllStudents() {
        List<Student> students = getAllStudents();
        if (students.isEmpty()) {
            resultArea.setText("No students found.");
        } else {
            StringBuilder sb = new StringBuilder("All Students:\n");
            for (Student student : students) {
                sb.append(student).append("\n");
            }
            resultArea.setText(sb.toString());
        }
    }

    private void addStudent() {
        String name = JOptionPane.showInputDialog("Enter student name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid name. Please try again.");
            return;
        }

        String ageStr = JOptionPane.showInputDialog("Enter student age:");
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid age. Please enter a valid number.");
            return;
        }

        try {
            // Insert new student into the database
            String insertSQL = "INSERT INTO students (name, age) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, age);
                preparedStatement.executeUpdate();

                // Retrieve the auto-generated ID of the new student
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    JOptionPane.showMessageDialog(this, "Student added successfully. ID: " + id);
                } else {
                    JOptionPane.showMessageDialog(this, "Error retrieving student ID.");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + e.getMessage());
        }
    }

    private List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();

        try {
            // Retrieve all students from the database
            String selectSQL = "SELECT * FROM students";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(selectSQL)) {

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    students.add(new Student(id, name, age));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentManagementSystemMySQL());
    }
}

class Student {
    private int id;
    private String name;
    private int age;

    public Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
