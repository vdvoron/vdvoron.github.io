This repository is my final Capstone project, the Inventory Management Android App,  which includes all documentation and code review materials. It includes the original version of the app, three written enhancement narratives, and a recorded code review, and each enhancement branch in GitHub contains the full Android Studio project for that version. Together, these files show the full progress of my work: from the first version to the final improved one, highlighting my growth in software engineering, database design, testing, and algorithmic problem-solving.

## **Inventory Management Android App – Capstone Project

The Inventory Management App is an Android application that lets users create accounts, log in, and manage their personal inventory. Each user can add, edit, delete, and search for items in their own list. The app saves all data through the Room database, so everything stays stored even after closing the app. It also includes system messages that work like alerts or confirmations, and SMS notifications that can be sent to the user’s phone when changes happen in the inventory.

## Repository Structure

In this main branch, there are documents and archives related to the Capstone submission:
•	Inventory App_Original File.zip – The very first version of the Inventory App before any improvements were made.
•	Enhancement One.docx – Narrative describing the first enhancement, which added automated JUnit and Espresso tests and improved input validation.
•	Enhancement Two.docx – Narrative describing the second enhancement, which added sorting by name or quantity, a binary search for faster lookups, and a HashMap index for instant searches.
•	Enhancement Three.docx – Narrative describing the final enhancement, where I rebuilt the Room database to include users, inventory, and system messages, and made system alerts appear in the user’s inbox.
•	Final Project Code Review.zip – My recorded walkthrough showing how the app works and how each enhancement improved it.
•	README.md – The main description file for the project and ePortfolio.
In addition to these files, the GitHub repository also has three enhancement branches:
•	Enhancement-One
•	Enhancement-Two
•	Enhancement-Three
Each branch contains the full Android Studio project for that version of the app. This structure helps show the progress from the original version to the final one.
App Structure (Inside Each Enhancement Branch)

Inside each enhancement branch, you can find the complete Android Studio project. The main code is located in:
app/src/main/java/com/zybooks/voronova_option1_final/
This folder contains all core classes for the application:
•	AppDatabase.java – Defines the Room database that connects all tables, including users, inventory items, and system messages.
•	User.java / UserDao.java – Handles account creation, login, and user queries.
•	InventoryItem.java / InventoryDao.java – Manages inventory items, including adding, updating, deleting, and retrieving.
•	Message.java / MessageDao.java – Stores system notifications and inbox messages linked to user actions.
•	MainActivity.java – Handles the login screen and navigation to the inventory page.
•	InventoryActivity.java – Displays all inventory items and allows item management.
•	InventoryAlgorithms.java – Implements sorting, searching (binary search), and HashMap indexing for faster lookups.
•	InventoryValidator.java – Checks user input for empty or invalid fields before saving.
•	SmsActivity.java – Manages SMS permissions, phone number input, and message sending.
Test files are located under:
app/src/test/java/com/zybooks/voronova_option1_final/ and
app/src/androidTest/java/com/zybooks/voronova_option1_final/
These folders include both JUnit and Espresso tests.
The res folder contains all XML resources:
•	layout/ – Activity layouts (activity_main.xml, activity_inventory.xml, activity_sms.xml)
•	values/ – Color, string, and theme resources
•	drawable/ and mipmap/ – Icons and images
Technologies Used
This app was built with Java in Android Studio. It uses the Android SDK and XML layouts for the interface, the Room database for data persistence, and SharedPreferences for storing local settings. I used JUnit and Espresso for testing, and GitHub for version control and documentation. SMS Manager was used for sending alert messages.
Summary

This project shows the full development process of an Android app from a simple starting version to a complete, stable, and well-structured product. Each enhancement focused on a different area of improvement, testing, performance, and database design, which together show my ability to apply software engineering, data management, and algorithmic thinking in real development work. The main branch now serves as a professional portfolio presentation of all those stages in one place.


<img width="468" height="637" alt="image" src="https://github.com/user-attachments/assets/7ce01609-d6cf-4dc2-a300-a1084351508a" />
