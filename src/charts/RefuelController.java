package charts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.Label;

public class RefuelController {
    // ArrayList of RefuelModel objects will be storing all data from the input file
    private ArrayList<RefuelModel> refuelings = new ArrayList<RefuelModel>();
    // months in an array
    private String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @FXML
    private TextField tbInputFile;
    @FXML
    private Button btnProcessData;
    @FXML
    private ComboBox<String> cbFuelType;
    @FXML
    private BarChart<String, Number> chartOutput;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private CategoryAxis yAxis;
    @FXML
    private Label lblErrorMessage;
    @FXML
    private Label lblFuelType;

    // Button "Process data" is clicked
    @FXML
    public void processData(ActionEvent event)
    {
        btnProcessData.setText("Processing...");

        // if data is valid, read the input file and initialize the filter (fuel types)
        if (!validateFields()) {
            setError("inputDataNotSpecified");
        }
        else {
            // get data from the input file
            this.refuelings = getRefuelings();

            if (this.refuelings != null)
            {
                // initialize ComboBox with fuel types
                initFilter(this.refuelings);

                // clear error message
                clearErrors();
            }
        }

        btnProcessData.setText("Process data");
    }

    // filter (fuel types) was selected
    @FXML
    public void selectFuelType(ActionEvent event)
    {
        // clear BarChart
        chartOutput.getData().clear();

        // get selected fuel type
        String selectedFuelType = cbFuelType.getSelectionModel().getSelectedItem().toString();

        // filter data by selected fuel type
        ArrayList<RefuelModel> filteredRefuelings = filterRefuelingsByType(this.refuelings, selectedFuelType);

        // populate the BarChart
        popupateBarChartReport(filteredRefuelings, selectedFuelType);
    }

    // validate the input data
    private boolean validateFields()
    {
        boolean isValid = false;

        // if INPUT FILE is specified, check if the file exists and it is in the right format
        // else input data was not specified, show an error message
        if (!tbInputFile.getText().isEmpty())
        {
            File file = new File(tbInputFile.getText());

            if (file.isFile())
            {
                file = file.getAbsoluteFile();

                if (file.exists()){
                    isValid = true;
                }
                else {
                    setError("inputDataPathNotCorrect");
                }
            }
            else {
                setError("inputDataPathNotCorrect");
            }
        }
        else {
            setError("inputDataNotSpecified");
        }

        return isValid;
    }

    // get all refueling records in ArrayList
    private ArrayList<RefuelModel> getRefuelings()
    {
        ArrayList<RefuelModel> refuelings = new ArrayList<RefuelModel>();

        File file = new File(tbInputFile.getText());
        Path path = file.toPath();

        try (Scanner s = new Scanner(path)) {
            while (s.hasNext()) {
                // data in each line is divided by symbol "|"
                String[] line = s.next().split("\\|");

                // set the price and amount to the proper format
                String priceFormatted = line[1].replace(",", ".");
                String amountFormatted = line[2].replace(",", ".");

                // if negative value, show an error
                if (Double.parseDouble(priceFormatted) < 0)
                    throw new IOException();

                // if negative value, show an error
                if (Double.parseDouble(amountFormatted) < 0)
                    throw new IOException();

                // find a month from the date string
                String[] calendar = line[3].split("\\.");
                int month = Integer.parseInt(calendar[1]);

                // add to ArrayList
                refuelings.add(new RefuelModel(line[0], Double.parseDouble(priceFormatted), Double.parseDouble(amountFormatted), month));
            }

            return refuelings;
        } catch (IOException e) {
            setError("dateParseError");

            return null;
        }
    }

    // get the fuel types without duplicates
    private ObservableList<String> getFuelTypes(ArrayList<RefuelModel> refuelings)
    {
        ObservableList<String> data = FXCollections.observableArrayList();

        for (RefuelModel model : refuelings) {
            String fuelName = model.getName();

            if (!data.contains(fuelName))
                data.add(fuelName);
        }

        data.add("all");

        return data;
    }

    // return refueling records by fuel type
    private ArrayList<RefuelModel> filterRefuelingsByType(ArrayList<RefuelModel> refuelings, String refuelType)
    {
        if (refuelType.equals("all"))
            return refuelings;

        ArrayList<RefuelModel> filteredRefuelings = new ArrayList<RefuelModel>();

        for (RefuelModel model : refuelings) {
            if (model.getName().equals(refuelType))
                filteredRefuelings.add(model);
        }

        return filteredRefuelings;
    }

    // populate the visual report in BarChart by selected fuel type
    private void popupateBarChartReport(ArrayList<RefuelModel> refuelings, String selectedFuelType)
    {
        // sort refuelings (from January to December)
        ArrayList<RefuelModel> refuelingsSorted = sortRefuelings(refuelings, selectedFuelType);

        // find maximal and minimal values
        double maxValue = findMaxValue(refuelingsSorted);
        double minValue = findMinValue(refuelingsSorted, maxValue);

        ObservableList<XYChart.Series<String, Number>> barChartData = FXCollections.observableArrayList();
        final BarChart.Series<String, Number> refuelingSeries =  new BarChart.Series<String, Number>();

        refuelingSeries.setName("Refuelings by months");
        String monthName = "";
        double monthPrice = 0.000;
        for (RefuelModel model : refuelingsSorted) {
            monthName = getMonthNameByNumber(model.getMonth());
            monthPrice = model.getPrice() * model.getAmount();

            DecimalFormat numberFormat = new DecimalFormat("0.000");
            Text dataText = new Text(numberFormat.format(monthPrice) + " ");

            final XYChart.Data<String, Number> data = new Data<String, Number>(monthName, monthPrice);
            data.nodeProperty().addListener(new ChangeListener<Node>() {
                @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                    setNodeStyle(data, maxValue, minValue);

                    displayLabelForData(data, dataText);
                }
            });

            refuelingSeries.getData().add(data);
        }

        barChartData.add(refuelingSeries);
        chartOutput.setData(barChartData);
    }

    // sort refuelings from January to February
    private ArrayList<RefuelModel> sortRefuelings(ArrayList<RefuelModel> refuelings, String selectedFuelType)
    {
        ArrayList<RefuelModel> refuelingsSorted = new ArrayList<RefuelModel>();

        // populate an array with empty RefuelModel objects, one for each month
        for (int monthCount = 1; monthCount < 13; monthCount++) {
            RefuelModel model = new RefuelModel(selectedFuelType, 0.000, 0.000, monthCount);
            refuelingsSorted.add(model);
        }

        // for each refueling, calculate and update the month
        for (RefuelModel model : refuelings) {
            int index = model.getMonth() - 1;
            RefuelModel updatedRefuelModel = refuelingsSorted.get(index);
            updatedRefuelModel.addPrice(model.getPrice());
            updatedRefuelModel.addAmount(model.getAmount());
        }

        return refuelingsSorted;
    }

    // find minimal value
    private double findMinValue(ArrayList<RefuelModel> refuelingsSorted, double maxValue) {
        double minValue = maxValue;

        double sum = 0;
        for (RefuelModel model : refuelingsSorted) {
            sum = model.getAmount() * model.getPrice();
            if (sum < minValue && sum != 0)
                minValue = sum;
        }

        return minValue;
    }

    // find maximal value
    private double findMaxValue(ArrayList<RefuelModel> refuelingsSorted) {
        double maxValue = 0;

        double sum = 0;
        for (RefuelModel model : refuelingsSorted) {
            sum = model.getAmount() * model.getPrice();
            if (sum > maxValue)
                maxValue = sum;
        }

        return maxValue;
    }

    // set color for each bar according to their value
    private void setNodeStyle(XYChart.Data<String, Number> data, double maxValue, double minValue) {
        Node node = data.getNode();

        if (data.getYValue().doubleValue() == maxValue) {
            node.setStyle("-fx-bar-fill: -maximum-values;");
        } else if (data.getYValue().doubleValue() == minValue) {
            node.setStyle("-fx-bar-fill: -minimum-values;");
        } else {
            node.setStyle("-fx-bar-fill: -average-values;");
        }
    }

    // set the price over each bar
    private void displayLabelForData(XYChart.Data<String, Number> data, Text dataText)
    {
        final Node node = data.getNode();

        node.parentProperty().addListener(new ChangeListener<Parent>() {
            @Override public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
                if (parent != null) {
                    Group parentGroup = (Group) parent;

                    parentGroup.getChildren().add(dataText);
                }
            }
        });

        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                dataText.setLayoutX(Math.round(bounds.getMinX() + bounds.getWidth() / 2 - dataText.prefWidth(-1) / 2));
                dataText.setLayoutY(Math.round(bounds.getMinY() - dataText.prefHeight(-1) * 0.5));
            }
        });
    }

    // initialize filter with the fuel types and enable it
    private void initFilter(ArrayList<RefuelModel> refuelings)
    {
        cbFuelType.setItems(getFuelTypes(refuelings));
        cbFuelType.setDisable(false);
        lblFuelType.setDisable(false);
    }

    // setting errors by alias
    private void setError(String errorAlias)
    {
        cbFuelType.setDisable(true);
        lblFuelType.setDisable(true);
        cbFuelType.getItems().clear();
        chartOutput.getData().clear();

        switch (errorAlias) {
            case "inputDataNotSpecified":
                lblErrorMessage.setText("Please, specify the input data.");
                break;
            case "inputDataPathNotCorrect":
                lblErrorMessage.setText("An input file or its path is specified incorrect.");
                break;
            case "dateParseError":
                lblErrorMessage.setText("Some input data are in a wrong format (negative value, wrong date format etc).");
                break;
        }
    }

    // clear errors
    private void clearErrors() {
        if (!lblErrorMessage.getText().isEmpty())
            lblErrorMessage.setText("");
    }

    // get month name by number
    private String getMonthNameByNumber(int month)
    {
        return months[month - 1];
    }
}
