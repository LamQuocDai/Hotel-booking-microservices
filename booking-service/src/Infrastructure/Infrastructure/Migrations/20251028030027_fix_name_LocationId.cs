using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Infrashtructure.Migrations
{
    /// <inheritdoc />
    public partial class fix_name_LocationId : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_rooms_locations_location",
                table: "rooms");

            migrationBuilder.RenameColumn(
                name: "location",
                table: "rooms",
                newName: "location_id");

            migrationBuilder.AddForeignKey(
                name: "fk_rooms_locations_location_id",
                table: "rooms",
                column: "location_id",
                principalTable: "locations",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_rooms_locations_location_id",
                table: "rooms");

            migrationBuilder.RenameColumn(
                name: "location_id",
                table: "rooms",
                newName: "location");

            migrationBuilder.AddForeignKey(
                name: "fk_rooms_locations_location",
                table: "rooms",
                column: "location",
                principalTable: "locations",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
