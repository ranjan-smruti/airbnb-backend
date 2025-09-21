package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.InventoryDTO;
import com.codingshuttle.projects.airBnbApp.DTO.UpdateInventoryRequestDTO;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ApiResponse;
import com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler.APIResponse;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<APIResponse<?>> updateInventory(@PathVariable Long roomId,
                                                @RequestBody UpdateInventoryRequestDTO updateInventoryRequestDTO){
        inventoryService.updateInventory(roomId,updateInventoryRequestDTO);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Inventories updated successfully for room id " + roomId)
                .build();
        return buildResponseEntity(apiResponse);
    }

    private ResponseEntity<APIResponse<?>> buildResponseEntity(ApiResponse apiResponse) {
        return new ResponseEntity<>(new APIResponse<>(apiResponse),apiResponse.getStatus());
    }
}
