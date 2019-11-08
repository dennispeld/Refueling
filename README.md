# Refueling
The *Refueling* project was written in Java programming language, using JavaFX library and JetBrains IntelliJ IDEA.

### Description
Refueling is a graphic Java application, which reads data on start-up from an input file containing information about refuelings.
For the test purpose, I have created a file refuelings.2016.txt with the data in this format:
```
fuelName|fuelPrice|fuelAmount|refuellingDate
```
where
```
fuelName - a string
fuelPrice - can be with . or , (1.345; 1,345)
fuelAmount - can be with . or , (50.53; 50,53)
refuellingDate - format is dd.mm.yyyy
```
For example:
```
98|1.392|45.25|11.03.2016
95|1.319|5.00|01.04.2016
D|1.219|5.00|01.02.2016
```

### Implementation
I have created JavaFX project, built a UI in SceneBuilder, written a Model that represents the data (fuel name, price, amount, date).  
For the main business logic, I have created a controller, where the data from the input file is read and saved in an array of objects (Model), filters it by selected fuel type, sorts by month and populates the BarChart.  
For the BarChart bars, I needed to find minimal and maximal values by fuel type to be able to show the right color of the bar.  

### Usage
1. Clone the project
2. Make sure you have configured JavaFX library in your project settings in whatever developing environment you use
3. Run the Main method of charts/Main file, which will open the app
4. Paste the path to refuelings.2016.txt file in the input field 
5. Click on the Process Data button
6. Select fuel type from the drop-down list
7. Observe the chart

Author: Dennis Peld  
Language: Java, SDK 13.0.1  
Additionally installed library: JavaFX  
Environment: JetBrains IntelliJ IDEA